package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenalishKnightTest {

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
    @DisplayName("Benalish Knight has correct card properties")
    void hasCorrectProperties() {
        BenalishKnight card = new BenalishKnight();

        assertThat(card.getName()).isEqualTo("Benalish Knight");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).containsExactlyInAnyOrder(Keyword.FLASH, Keyword.FIRST_STRIKE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.KNIGHT);
    }

    // ===== Casting during main phase =====

    @Test
    @DisplayName("Can cast during main phase like a normal creature")
    void canCastDuringMainPhase() {
        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Benalish Knight");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Flash — casting at instant speed =====

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        // Player2 passes priority, giving player1 priority
        harness.getGameService().passPriority(harness.getGameData(), player2);

        // Player1 can cast with Flash even though it's not their turn
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Benalish Knight");
    }

    @Test
    @DisplayName("Can cast during combat step thanks to Flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Benalish Knight");
    }

    @Test
    @DisplayName("Non-flash creature cannot be cast during combat step")
    void nonFlashCreatureCannotCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Benalish Knight onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Benalish Knight"));
    }

    @Test
    @DisplayName("Benalish Knight has first strike and flash on the battlefield")
    void hasKeywordsOnBattlefield() {
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(perm.hasKeyword(Keyword.FLASH)).isTrue();
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("First strike kills a 2/2 before it deals regular damage")
    void firstStrikeKillsBeforeRegularDamage() {
        // Benalish Knight (2/2 first strike) attacks, blocked by Grizzly Bears (2/2)
        BenalishKnight knight = new BenalishKnight();
        Permanent attacker = new Permanent(knight);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Benalish Knight deals 2 first strike damage → kills Grizzly Bears before it can deal damage
        // Benalish Knight survives with 0 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Benalish Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("First strike creature still dies if blocker survives first strike")
    void firstStrikeCreatureDiesIfBlockerSurvives() {
        // Benalish Knight (2/2 first strike) attacks, blocked by Angel of Mercy (3/3)
        // First strike deals 2 damage → Angel survives (2 < 3)
        // Regular damage: Angel deals 3 → Benalish Knight dies (3 >= 2)
        BenalishKnight knight = new BenalishKnight();
        Permanent attacker = new Permanent(knight);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        // Use a 3/3 creature to survive first strike
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Blocker survives first strike (2 < 3 toughness), then deals 3 damage → knight dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Benalish Knight"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Benalish Knight"));
        // Blocker survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Benalish Knight enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Knight"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }
}

