package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PalaceJailer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarscreamPowerHungry.class, StarscreamSeekerLeader.class, GrizzlyBears.class,
        PalaceJailer.class})
class StarscreamPowerHungryTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsSeekerLeaderWithLivingMetal() {
        Permanent starscream = castConvertedStarscream();

        assertThat(starscream.isTransformed()).isTrue();
        assertThat(starscream.getCard()).isInstanceOf(StarscreamSeekerLeader.class);
        assertThat(gqs.isCreature(gd, starscream)).isTrue();
    }

    @Test
    void monarchDrawTriggerMakesTargetOpponentLoseLife() {
        harness.addToBattlefield(player1, new StarscreamPowerHungry());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        gd.monarchPlayerId = player1.getId();

        drawCard();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void monarchDrawTriggerDoesNotFireWhenControllerIsNotMonarch() {
        harness.addToBattlefield(player1, new StarscreamPowerHungry());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        gd.monarchPlayerId = player2.getId();

        drawCard();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void frontFaceConvertsWhenACreatureDealsCombatDamageToItsController() {
        Permanent starscream = harness.addToBattlefieldAndReturn(player1, new StarscreamPowerHungry());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(starscream.isTransformed()).isTrue();
        assertThat(starscream.getCard()).isInstanceOf(StarscreamSeekerLeader.class);
    }

    @Test
    void backFaceCombatDamageMakesDamagedPlayerMonarchWhenThereIsNoMonarch() {
        Permanent starscream = castConvertedStarscream();
        starscream.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        gd.interaction.clearAwaitingInput();
        starscream.setAttacking(true);
        starscream.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        assertThat(starscream.isTransformed()).isTrue();
    }

    @Test
    void backFaceConvertsWhenItsControllerBecomesMonarch() {
        Permanent starscream = castConvertedStarscream();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PalaceJailer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(starscream.isTransformed()).isFalse();
        assertThat(starscream.getCard()).isInstanceOf(StarscreamPowerHungry.class);
    }

    private Permanent castConvertedStarscream() {
        harness.setHand(player1, List.of(new StarscreamPowerHungry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Starscream, Seeker Leader");
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
