package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FormAPosse.class, GrizzlyBears.class})
class FormAPosseTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X Mercenary tokens")
    void createsXMercenaryTokens() {
        castFormAPosse(3);

        List<Permanent> mercenaries = findPermanents(player1, "Mercenary");
        assertThat(mercenaries).hasSize(3);
        assertThat(mercenaries).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Mercenary tokens boost a creature you control at sorcery speed")
    void mercenaryTokenBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castFormAPosse(1);
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mercenary tokens cannot activate their ability outside sorcery speed")
    void mercenaryTokenRequiresSorcerySpeed() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castFormAPosse(1);
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void zeroXCreatesNoTokens() {
        castFormAPosse(0);

        assertThat(findPermanents(player1, "Mercenary")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyCreatedMercenaryCannotPayTapCost() {
        castFormAPosse(1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mercenaryCanBoostItselfAndBoostExpiresAtEndOfTurn() {
        castFormAPosse(1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, index, 0, null, mercenary.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
    }

    @Test
    void mercenaryCannotTargetOpponentsCreature() {
        castFormAPosse(1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FormAPosse()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, 1);
        Permanent opponentMercenary = findPermanent(player2, "Mercenary");
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, opponentMercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mercenaryCannotActivateDuringOpponentsMainPhase() {
        castFormAPosse(1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotActivateWithSpellOnStack() {
        castFormAPosse(1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.setHand(player1, List.of(new FormAPosse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private void castFormAPosse(int xValue) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FormAPosse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
