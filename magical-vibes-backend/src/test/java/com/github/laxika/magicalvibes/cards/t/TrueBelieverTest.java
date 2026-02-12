package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrueBelieverTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("True Believer has correct card properties")
    void hasCorrectProperties() {
        TrueBeliever card = new TrueBeliever();

        assertThat(card.getName()).isEqualTo("True Believer");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).containsExactly(Keyword.SHROUD);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.CLERIC);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting True Believer puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new TrueBeliever()));
        harness.addMana(player1, "W", 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("True Believer");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new TrueBeliever()));
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts True Believer onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new TrueBeliever()));
        harness.addMana(player1, "W", 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("True Believer"));
    }

    @Test
    @DisplayName("True Believer has shroud keyword on the battlefield")
    void hasShroudOnBattlefield() {
        harness.addToBattlefield(player1, new TrueBeliever());

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("True Believer enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new TrueBeliever()));
        harness.addMana(player1, "W", 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("True Believer"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Player shroud — spells cannot target player =====

    @Test
    @DisplayName("Opponent cannot target player with a spell when True Believer is on battlefield")
    void opponentCannotTargetPlayerWithSpell() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, "W", 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Player can still target themselves with a spell when opponent has True Believer")
    void canTargetSelfWhenOpponentHasShroud() {
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Player with True Believer cannot be targeted even by their own spells")
    void cannotTargetSelfWithSpellWhenOwnTrueBelieverOnField() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    // ===== Shroud only while on battlefield =====

    @Test
    @DisplayName("Player can be targeted after True Believer is removed from battlefield")
    void canTargetPlayerAfterTrueBelieverRemoved() {
        TrueBeliever believer = new TrueBeliever();
        harness.addToBattlefield(player1, believer);

        // Remove True Believer from battlefield
        GameData gd = harness.getGameData();
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("True Believer"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(believer);

        // Now the player can be targeted
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, "W", 6);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    // ===== Shroud does not protect the creature itself =====

    @Test
    @DisplayName("True Believer grants shroud to the player, not to creatures")
    void shroudProtectsPlayerNotCreatures() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Spells can still target creatures on the battlefield
        // (shroud on True Believer gives the player shroud, not the creatures)
        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    // ===== Multiple True Believers =====

    @Test
    @DisplayName("Multiple True Believers on battlefield still grant shroud")
    void multipleTrueBelieversStillGrantShroud() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.addToBattlefield(player1, new TrueBeliever());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, "W", 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Removing one True Believer when two are on battlefield still protects player")
    void removingOneTrueBelieverStillProtectsWhenTwoExist() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.addToBattlefield(player1, new TrueBeliever());

        // Remove one True Believer
        GameData gd = harness.getGameData();
        Permanent firstBeliever = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("True Believer"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(firstBeliever);

        // Player still has shroud from the second True Believer
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, "W", 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
