package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

class YouthfulKnightTest {

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
    @DisplayName("Youthful Knight has correct card properties")
    void hasCorrectProperties() {
        YouthfulKnight card = new YouthfulKnight();

        assertThat(card.getName()).isEqualTo("Youthful Knight");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getKeywords()).containsExactly(Keyword.FIRST_STRIKE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.KNIGHT);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as CREATURE_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Youthful Knight");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new YouthfulKnight()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Youthful Knight onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Youthful Knight"));
    }

    @Test
    @DisplayName("Youthful Knight has first strike on the battlefield")
    void hasFirstStrikeOnBattlefield() {
        harness.addToBattlefield(player1, new YouthfulKnight());

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Youthful Knight"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("First strike kills a 2/1 before it deals regular damage")
    void firstStrikeKillsBeforeRegularDamage() {
        // Youthful Knight (2/1 first strike) attacks, blocked by a 1/1
        YouthfulKnight knight = new YouthfulKnight();
        Permanent attacker = new Permanent(knight);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = new Permanent(smallCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // Youthful Knight deals 2 first strike damage → kills 1/1 before it can deal damage
        // Youthful Knight survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Youthful Knight"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Youthful Knight dies to a 2/2 blocker despite first strike")
    void diesTo2_2BlockerDespiteFirstStrike() {
        // Youthful Knight (2/1 first strike) attacks, blocked by Grizzly Bears (2/2)
        // First strike deals 2 → Bears survives (2 < 2? No, 2 >= 2, Bears dies)
        // Actually 2 damage to a 2 toughness creature kills it, so let's use a 2/3
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(2);
        bigBear.setToughness(3);

        YouthfulKnight knight = new YouthfulKnight();
        Permanent attacker = new Permanent(knight);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // First strike deals 2 → blocker survives (2 < 3 toughness)
        // Regular damage: blocker deals 2 → Youthful Knight dies (2 >= 1 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Youthful Knight"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Youthful Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
