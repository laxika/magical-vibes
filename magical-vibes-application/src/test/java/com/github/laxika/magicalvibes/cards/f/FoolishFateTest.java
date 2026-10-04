package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HungryGraffalon;
import com.github.laxika.magicalvibes.cards.m.MasterfulFlourish;
import com.github.laxika.magicalvibes.cards.o.OraclesRestoration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoolishFate.class, HungryGraffalon.class, MasterfulFlourish.class, OraclesRestoration.class})
class FoolishFateTest extends BaseCardTest {

    @Test
    @DisplayName("Without life gained this turn, only destroys the target creature")
    void withoutLifeGainOnlyDestroys() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("If you gained life this turn, destroys the creature and its controller loses 3 life")
    void withLifeGainDestroysAndDrains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertLife(player2, 17);
    }

    @Test
    void lifeGainedAfterCastingEnablesInfusionAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsLifeGainDoesNotEnableInfusion() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player2.getId(), 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertLife(player2, 20);
    }

    @Test
    void targetingOwnCreatureMakesCasterLoseLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Hungry Graffalon");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void indestructibleCreatureSurvivesButItsControllerStillLosesLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player2, List.of(new MasterfulFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Hungry Graffalon");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void regeneratedCreatureSurvivesButItsControllerStillLosesLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        creature.setRegenerationShield(1);
        harness.setHand(player1, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Hungry Graffalon");
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    void removedTargetPreventsInfusionLifeLoss() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new FoolishFate()));
        harness.setHand(player2, List.of(new FoolishFate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertInGraveyard(player1, "Foolish Fate");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    void actualLifeGainEnablesInfusionEvenAfterLifeTotalFalls() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new OraclesRestoration(), new FoolishFate()));
        harness.setLibrary(player1, List.of(new HungryGraffalon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, ownCreature.getId());
        harness.assertLife(player1, 21);
        harness.setLife(player1, 15);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 15);
    }
}
