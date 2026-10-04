package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ContentiousPlan;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IchorAberration.class, ContentiousPlan.class})
class IchorAberrationTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferating perpetually boosts Ichor Aberration on the battlefield")
    void proliferatingPerpetuallyBoostsBattlefieldAberration() {
        Permanent aberration = addReadyAberration();
        castContentiousPlan();

        assertThat(gqs.getEffectivePower(gd, aberration)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aberration)).isEqualTo(4);
    }

    @Test
    @DisplayName("Proliferating in the graveyard boosts Ichor Aberration and permits casting it")
    void proliferatingBoostsAndPermitsGraveyardCast() {
        IchorAberration aberration = new IchorAberration();
        harness.setGraveyard(player1, List.of(aberration));
        castContentiousPlan();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(aberration.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ichor Aberration can attack only once its power is at least seven")
    void attacksOnlyAtSevenPower() {
        Permanent aberration = addReadyAberration();
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        aberration.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0)));

        assertThat(aberration.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A battlefield proliferate trigger permits casting after the creature dies later that turn")
    void battlefieldTriggerPermitsCastingAfterLaterDeath() {
        Permanent aberration = addReadyAberration();
        castContentiousPlan();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, aberration));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("The proliferate trigger does nothing if Ichor Aberration is exiled before resolution")
    void exilingBeforeTriggerResolutionPreventsPerpetualBoost() {
        Permanent aberration = addReadyAberration();
        harness.setHand(player1, List.of(new ContentiousPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, aberration));
        harness.passBothPriorities();

        var exiled = gd.findExiledCard(aberration.getCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(gqs.getEffectiveCardPower(gd, exiled.card())).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, exiled.card())).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated proliferations accumulate perpetual boosts without counters")
    void repeatedProliferationsAccumulateBoosts() {
        Permanent aberration = addReadyAberration();
        castContentiousPlan();
        castContentiousPlan();

        assertThat(gqs.getEffectivePower(gd, aberration)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aberration)).isEqualTo(5);
        assertThat(aberration.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent proliferating does not trigger Ichor Aberration")
    void opponentProliferatingDoesNotBoostAberration() {
        Permanent aberration = addReadyAberration();
        IchorAberration graveyardAberration = new IchorAberration();
        harness.setGraveyard(player1, List.of(graveyardAberration));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ContentiousPlan()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, aberration)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardPower(gd, graveyardAberration)).isEqualTo(3);
    }

    @Test
    @DisplayName("Six power is still insufficient to attack through defender")
    void cannotAttackAtSixPower() {
        Permanent aberration = addReadyAberration();
        aberration.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(aberration.isAttacking()).isFalse();
    }

    private Permanent addReadyAberration() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new IchorAberration());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castContentiousPlan() {
        harness.setHand(player1, List.of(new ContentiousPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
