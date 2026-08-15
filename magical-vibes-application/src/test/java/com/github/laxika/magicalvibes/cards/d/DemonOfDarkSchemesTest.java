package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemonOfDarkSchemesTest extends BaseCardTest {

    @Test
    void etbGivesOtherCreaturesMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new KalonianTusker());
        harness.setHand(player1, List.of(new DemonOfDarkSchemes()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Demon of Dark Schemes").getEffectivePower()).isEqualTo(5);
        assertThat(findPermanent(player1, "Demon of Dark Schemes").getEffectiveToughness()).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player2, "Kalonian Tusker").getEffectivePower()).isEqualTo(1);
        assertThat(findPermanent(player2, "Kalonian Tusker").getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void gainsEnergyWhenAnotherCreatureDies() {
        addReadyDemon();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void ownDeathDoesNotGrantEnergy() {
        addReadyDemon();
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Demon of Dark Schemes"));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsTargetCreatureFromAnyGraveyardTappedAndPaysEnergy() {
        int demonIndex = addReadyDemon();
        Card target = new KalonianTusker();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbilityWithGraveyardTargets(player1, demonIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Kalonian Tusker");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertNotInGraveyard(player2, "Kalonian Tusker");
    }

    @Test
    void cannotTargetNonCreatureCardInGraveyard() {
        int demonIndex = addReadyDemon();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerEnergyCounters.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, demonIndex, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int addReadyDemon() {
        harness.addToBattlefield(player1, new DemonOfDarkSchemes());
        Permanent demon = findPermanent(player1, "Demon of Dark Schemes");
        demon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).indexOf(demon);
    }
}
