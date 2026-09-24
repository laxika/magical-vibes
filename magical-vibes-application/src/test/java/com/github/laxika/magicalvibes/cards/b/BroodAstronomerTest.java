package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.CardType;
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
        harness.setHand(player1, List.of(new BroodAstronomer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.ChooseModeChoice.class);
        ChoiceContext.ChooseModeChoice context = (ChoiceContext.ChooseModeChoice) interaction.context();
        assertThat(context.effect().options()).hasSize(3);
        String chosenName = context.effect().options().getFirst().label();

        harness.handleListChoice(player1, chosenName);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        Permanent chosen = findPermanent(player1, chosenName);
        assertThat(chosen.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(chosen.isTapped()).isTrue();
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

        harness.addToBattlefield(player1, new EvendoWakingHaven());
        Permanent planet = findPermanent(player1, "Evendo, Waking Haven");
        planet.setCounterCount(CounterType.CHARGE, 12);
        astronomer.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }
}
