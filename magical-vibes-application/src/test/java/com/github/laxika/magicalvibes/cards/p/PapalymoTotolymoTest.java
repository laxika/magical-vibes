package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PapalymoTotolymo.class, GrizzlyBears.class, HillGiant.class, Opt.class})
class PapalymoTotolymoTest extends BaseCardTest {

    @Test
    void noncreatureSpellDealsDamageAndGainsLife() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void activatedAbilitySacrificesOpponentsGreatestPowerCreatureAfterLifeLoss() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void activatedAbilitySkipsOpponentsWhoDidNotLoseLife() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void creatureSpellDoesNotTriggerDamageOrLifeGain() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTrigger() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentChoosesOneOfTiedGreatestPowerCreatures() {
        addCreatureReady(player1, new PapalymoTotolymo());
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(secondGiant.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).contains(firstGiant.getId()).doesNotContain(secondGiant.getId());
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void losingLifeThenGainingLifeStillRequiresSacrifice() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.addToBattlefield(player2, new HillGiant());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLife(player2, 25);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player2, 25);
    }

    @Test
    void controllerLifeLossDoesNotMakeUnaffectedOpponentSacrifice() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void castTriggerLifeLossEnablesSacrificeAbility() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player2, 19);
    }

    @Test
    void opponentWithoutCreaturesHasNothingToSacrifice() {
        addCreatureReady(player1, new PapalymoTotolymo());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void greatestPowerUsesCurrentPowerWhenAbilityResolves() {
        addCreatureReady(player1, new PapalymoTotolymo());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        bears.setPowerModifier(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void lifeLossIsCheckedWhenAbilityResolves() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void castTriggerStillDealsDamageAfterPapalymoIsSacrificed() {
        addCreatureReady(player1, new PapalymoTotolymo());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Papalymo Totolymo");
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

}
