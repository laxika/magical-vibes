package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolatileOrbit.class, AdagiaWindsweptBastion.class, EvendoWakingHaven.class,
        KavaronMemorialWorld.class, SusurSecundiVoidAltar.class, UthrosTitanicGodcore.class})
class VolatileOrbitTest extends BaseCardTest {

    @Test
    void dealsDamageOnEntryIfYouControlAPlanet() {
        harness.addToBattlefield(player1, new EvendoWakingHaven());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new VolatileOrbit(), "{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotTriggerOnEntryWithoutAPlanet() {
        harness.castFromHand(player1, new VolatileOrbit(), "{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void sacrificesToChooseAnyPlanetWithEightChargeCounters() {
        Permanent orbit = harness.addToBattlefieldAndReturn(player1, new VolatileOrbit());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction).isNotNull();
        var context = (com.github.laxika.magicalvibes.model.ChoiceContext.ChooseModeChoice) interaction.context();
        assertThat(context.effect().options()).hasSize(5);

        String chosenName = context.effect().options().getFirst().label();
        harness.handleListChoice(player1, chosenName);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals(chosenName))
                .singleElement()
                .satisfies(planet -> {
                    assertThat(planet.isTapped()).isTrue();
                    assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
                });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orbit);
    }

    @Test
    void opponentsPlanetDoesNotEnableEntryTrigger() {
        harness.addToBattlefield(player2, new EvendoWakingHaven());
        harness.castFromHand(player1, new VolatileOrbit(), "{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDealDamageIfLastPlanetLeavesBeforeTriggerResolves() {
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        harness.castFromHand(player1, new VolatileOrbit(), "{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, planet));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void entryTriggerCanDamageItsControllerAfterOrbitLeaves() {
        harness.addToBattlefield(player1, new EvendoWakingHaven());
        harness.castFromHand(player1, new VolatileOrbit(), "{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        Permanent orbit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VolatileOrbit)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, orbit));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Adagia, Windswept Bastion", "Evendo, Waking Haven", "Kavaron, Memorial World",
            "Susur Secundi, Void Altar", "Uthros, Titanic Godcore"})
    void eachSpellbookPlanetEntersTappedWithEightCounters(String name) {
        harness.addToBattlefield(player1, new VolatileOrbit());
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Volatile Orbit");
        harness.assertNotOnBattlefield(player1, "Volatile Orbit");
        harness.passBothPriorities();
        harness.handleListChoice(player1, name);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(planet -> {
            assertThat(planet.getCard().getName()).isEqualTo(name);
            assertThat(planet.isTapped()).isTrue();
            assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
        });
    }

    @Test
    void conjuredPlanetDoesNotReceiveEightCountersWhenPlayedAgain() {
        harness.addToBattlefield(player1, new VolatileOrbit());
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Evendo, Waking Haven");
        Permanent planet = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(8);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, planet));
        harness.ensurePriority(player1);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(reentered -> {
            assertThat(reentered.isTapped()).isTrue();
            assertThat(reentered.getCounterCount(CounterType.CHARGE)).isZero();
        });
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent orbit = harness.addToBattlefieldAndReturn(player1, new VolatileOrbit());
        addActivationMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(orbit);
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        Permanent orbit = harness.addToBattlefieldAndReturn(player1, new VolatileOrbit());
        harness.castFromHand(player1, new VolatileOrbit(), "{R}{G}");
        addActivationMana();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(orbit);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
