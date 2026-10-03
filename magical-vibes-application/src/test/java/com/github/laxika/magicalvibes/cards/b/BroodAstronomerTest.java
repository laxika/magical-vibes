package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodAstronomer.class, Forest.class, AdagiaWindsweptBastion.class,
        EvendoWakingHaven.class, KavaronMemorialWorld.class, SusurSecundiVoidAltar.class,
        UthrosTitanicGodcore.class})
class BroodAstronomerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice a land to draft one of three Planets and put it tapped")
    void draftsPlanetAfterSacrificingLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new BroodAstronomer(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());

        PendingInteraction.ColorChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.ChooseModeChoice.class);
        ChoiceContext.ChooseModeChoice context = (ChoiceContext.ChooseModeChoice) interaction.context();
        assertThat(context.effect().options()).hasSize(3);
        assertThat(context.effect().options().stream().map(option -> option.label()).toList())
                .doesNotHaveDuplicates()
                .allMatch(List.of("Adagia, Windswept Bastion", "Evendo, Waking Haven",
                        "Kavaron, Memorial World", "Susur Secundi, Void Altar",
                        "Uthros, Titanic Godcore")::contains);
        String chosenName = context.effect().options().getFirst().label();

        harness.handleListChoice(player1, chosenName);

        harness.assertInGraveyard(player1, "Forest");
        Permanent chosen = findPermanent(player1, chosenName);
        assertThat(chosen.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, chosenName);
    }

    @Test
    @DisplayName("Mana ability adds one mana normally and three with a charged Planet")
    void manaAbilityUsesChargedPlanetCondition() {
        Permanent astronomer = harness.addToBattlefieldAndReturn(player1, new BroodAstronomer());
        astronomer.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        Permanent planet = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        planet.setCounterCount(CounterType.CHARGE, 12);
        astronomer.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the sacrifice keeps the land and does not draft a Planet")
    void decliningSacrificeDoesNotDraft() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new BroodAstronomer(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No land to sacrifice means no Planet is drafted")
    void noLandDoesNotDraft() {
        harness.castFromHand(player1, new BroodAstronomer(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Eleven counters produce one mana and more than twelve produce three")
    void manaThresholdUsesAtLeastTwelveCounters() {
        Permanent astronomer = harness.addToBattlefieldAndReturn(player1, new BroodAstronomer());
        astronomer.setSummoningSick(false);
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        planet.setCounterCount(CounterType.CHARGE, 11);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(astronomer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        astronomer.untap();
        planet.setCounterCount(CounterType.CHARGE, 13);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(astronomer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters must be on one Planet controlled by the mana ability's controller")
    void otherCountersDoNotMeetManaCondition() {
        Permanent astronomer = harness.addToBattlefieldAndReturn(player1, new BroodAstronomer());
        astronomer.setSummoningSick(false);
        Permanent firstPlanet = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent secondPlanet = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent opposingPlanet = harness.addToBattlefieldAndReturn(player2, new UthrosTitanicGodcore());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstPlanet.setCounterCount(CounterType.CHARGE, 6);
        secondPlanet.setCounterCount(CounterType.CHARGE, 6);
        opposingPlanet.setCounterCount(CounterType.CHARGE, 12);
        forest.setCounterCount(CounterType.CHARGE, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
