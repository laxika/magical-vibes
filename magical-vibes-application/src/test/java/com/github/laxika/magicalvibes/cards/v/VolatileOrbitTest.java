package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolatileOrbit.class, AdagiaWindsweptBastion.class, EvendoWakingHaven.class,
        KavaronMemorialWorld.class, SusurSecundiVoidAltar.class, UthrosTitanicGodcore.class})
class VolatileOrbitTest extends BaseCardTest {

    @Test
    void dealsDamageOnEntryIfYouControlAPlanet() {
        harness.addToBattlefield(player1, new EvendoWakingHaven());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolatileOrbit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotTriggerOnEntryWithoutAPlanet() {
        harness.setHand(player1, List.of(new VolatileOrbit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
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
                    assertThat(planet.getCard().hasType(CardType.LAND)).isTrue();
                    assertThat(planet.isTapped()).isTrue();
                    assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
                });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orbit);
    }
}
