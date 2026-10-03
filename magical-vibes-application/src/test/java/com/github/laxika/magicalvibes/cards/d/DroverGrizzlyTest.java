package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RevokePrivileges;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DroverGrizzly.class, RevokePrivileges.class})
class DroverGrizzlyTest extends BaseCardTest {

    @Test
    @DisplayName("Saddled attack gives creatures you control trample until end of turn")
    void saddledAttackGrantsTrampleToCreaturesYouControl() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new DroverGrizzly());
        Permanent opponentCreature = addCreatureReady(player2, new DroverGrizzly());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drover.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, drover, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drover, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isFalse();
        assertThat(drover.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Unsaddled attack does not grant trample")
    void unsaddledAttackDoesNotGrantTrample() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new DroverGrizzly());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, drover, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void summoningSickCreatureCanSaddle() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new DroverGrizzly());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(helper.isTapped()).isTrue();
        assertThat(drover.isSaddled()).isFalse();
        harness.passBothPriorities();

        assertThat(drover.isSaddled()).isTrue();
        assertThat(drover.isTapped()).isFalse();
    }

    @Test
    void cannotSaddleUsingItselfOrOpponentsCreature() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent opponent = addCreatureReady(player2, new DroverGrizzly());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drover.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(drover.isSaddled()).isFalse();
    }

    @Test
    void cannotSaddleDuringCombat() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new DroverGrizzly());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(drover.isSaddled()).isFalse();
    }

    @Test
    void attackTriggerSurvivesSourceLeavingAndOnlyGrantsToCreaturesPresentAtResolution() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new DroverGrizzly());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(drover);
        Permanent beforeResolution = addCreatureReady(player1, new DroverGrizzly());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new DroverGrizzly());

        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void creatureThatCannotCrewVehiclesCanStillSaddle() {
        Permanent drover = addCreatureReady(player1, new DroverGrizzly());
        Permanent helper = addCreatureReady(player1, new DroverGrizzly());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, helper.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(drover.isSaddled()).isTrue();
    }
}
