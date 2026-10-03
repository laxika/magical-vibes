package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrazenScourge;
import com.github.laxika.magicalvibes.cards.f.Fumigate;
import com.github.laxika.magicalvibes.cards.s.SelectForInspection;
import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonOfDarkSchemes.class, DhundOperative.class, BrazenScourge.class,
        TidyConclusion.class, SelectForInspection.class, Fumigate.class})
class DemonOfDarkSchemesTest extends BaseCardTest {

    @Test
    void etbGivesOtherCreaturesMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player2, new BrazenScourge());
        harness.setHand(player1, List.of(new DemonOfDarkSchemes()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Demon of Dark Schemes").getEffectivePower()).isEqualTo(5);
        assertThat(findPermanent(player1, "Demon of Dark Schemes").getEffectiveToughness()).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Dhund Operative");
        assertThat(findPermanent(player2, "Brazen Scourge").getEffectivePower()).isEqualTo(1);
        assertThat(findPermanent(player2, "Brazen Scourge").getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void gainsEnergyWhenAnotherCreatureDies() {
        addReadyDemon();
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new DhundOperative());
        harness.setHand(player1, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, operative.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void ownDeathDoesNotGrantEnergy() {
        addReadyDemon();
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Demon of Dark Schemes"));

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsTargetCreatureFromAnyGraveyardTappedAndPaysEnergy() {
        int demonIndex = addReadyDemon();
        Card target = new BrazenScourge();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbilityWithGraveyardTargets(player1, demonIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Brazen Scourge");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertNotInGraveyard(player2, "Brazen Scourge");
    }

    @Test
    void cannotTargetNonCreatureCardInGraveyard() {
        int demonIndex = addReadyDemon();
        Card nonCreature = new SelectForInspection();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, demonIndex, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void etbGrantsOneEnergyForEachCreatureItKillsOnEitherSide() {
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player2, new DhundOperative());
        harness.setHand(player1, List.of(new DemonOfDarkSchemes()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dhund Operative");
        harness.assertNotOnBattlefield(player2, "Dhund Operative");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsEnergyForOtherCreaturesDyingAtTheSameTimeAsIt() {
        addReadyDemon();
        harness.addToBattlefield(player1, new DhundOperative());
        harness.addToBattlefield(player2, new BrazenScourge());
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Demon of Dark Schemes");
        harness.assertNotOnBattlefield(player1, "Dhund Operative");
        harness.assertNotOnBattlefield(player2, "Brazen Scourge");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbDoesNotAffectCreaturesEnteringLaterAndExpiresAtEndOfTurn() {
        harness.addToBattlefield(player2, new BrazenScourge());
        harness.setHand(player1, List.of(new DemonOfDarkSchemes(), new DhundOperative()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent laterCreature = findPermanent(player1, "Dhund Operative");
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanent(player2, "Brazen Scourge").getEffectiveToughness()).isEqualTo(1);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(findPermanent(player2, "Brazen Scourge").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player2, "Brazen Scourge").getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void canReanimateFromOwnGraveyardWhileSummoningSickAndPaysEnergyImmediately() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfDarkSchemes());
        demon.setSummoningSick(true);
        Card target = new DhundOperative();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dhund Operative");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dhund Operative").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Dhund Operative");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotActivateWithFewerThanFourEnergy() {
        int demonIndex = addReadyDemon();
        Card target = new BrazenScourge();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, demonIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Brazen Scourge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reanimationResolvesAfterTheDemonIsDestroyed() {
        int demonIndex = addReadyDemon();
        Card target = new BrazenScourge();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 4);
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, demonIndex, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Demon of Dark Schemes"));
        harness.assertNotOnBattlefield(player1, "Demon of Dark Schemes");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Brazen Scourge").isTapped()).isTrue();
        harness.assertNotInGraveyard(player2, "Brazen Scourge");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void reanimationDoesNotRefundEnergyWhenAnotherActivationTakesItsTarget() {
        int demonIndex = addReadyDemon();
        Card target = new BrazenScourge();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 6);
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbilityWithGraveyardTargets(player1, demonIndex, 0, List.of(target.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, demonIndex, 0, List.of(target.getId()));
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Brazen Scourge");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Brazen Scourge").getId()).isEqualTo(returned.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private int addReadyDemon() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfDarkSchemes());
        demon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).indexOf(demon);
    }
}
