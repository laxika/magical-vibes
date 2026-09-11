package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.o.OdylicWraith;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishKnight.class, OdylicWraith.class})
class BenalishKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during main phase like a normal creature")
    void canCastDuringMainPhase() {
        BenalishKnight knight = new BenalishKnight();
        harness.castFromHand(player1, knight, "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isSameAs(knight);
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

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player2 passes priority, giving player1 priority.
        harness.getGameService().passPriority(harness.getGameData(), player2);

        // Player1 can cast with Flash even though it is not their turn.
        BenalishKnight knight = new BenalishKnight();
        harness.castFromHand(player1, knight, "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(knight);
    }

    @Test
    @DisplayName("Can cast during combat step thanks to Flash")
    void canCastDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        BenalishKnight knight = new BenalishKnight();
        harness.castFromHand(player1, knight, "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(knight);
    }

    @Test
    @DisplayName("Non-flash creature cannot be cast during combat step")
    void nonFlashCreatureCannotCastDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new OdylicWraith()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Benalish Knight onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new BenalishKnight(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BenalishKnight);
    }

    @Test
    @DisplayName("First strike kills a 2/2 before it deals regular damage")
    void firstStrikeKillsBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new OdylicWraith());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BenalishKnight);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof OdylicWraith);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof OdylicWraith);
    }

    @Test
    @DisplayName("First strike creature still dies if blocker survives first strike")
    void firstStrikeCreatureDiesIfBlockerSurvives() {
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());
        attacker.setAttacking(true);

        OdylicWraith blockerCard = new OdylicWraith();
        blockerCard.setPower(3);
        blockerCard.setToughness(3);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof BenalishKnight);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BenalishKnight);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof OdylicWraith);
    }

    @Test
    @DisplayName("Benalish Knight enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        BenalishKnight knight = new BenalishKnight();
        harness.castFromHand(player1, knight, "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.getCard()).isSameAs(knight);
        assertThat(perm.isSummoningSick()).isTrue();
    }
}
