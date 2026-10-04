package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheWarDoctor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnsnaredByTheMaraVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnsnaredByTheMara.class, Forest.class, GrizzlyBears.class, TheWarDoctor.class})
class EnsnaredByTheMaraTest extends BaseCardTest {

    @Test
    void opponentCanChooseFreeCastFromTheirLibrary() {
        Forest exiledLand = new Forest();
        GrizzlyBears exiledSpell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledLand, exiledSpell));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledLand);
    }

    @Test
    void decliningFreeCastLeavesTheNonlandCardExiled() {
        GrizzlyBears exiledSpell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledSpell));

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledSpell);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentCanChooseExileFourAndTakeTotalManaValueDamage() {
        List<com.github.laxika.magicalvibes.model.Card> exiledCards = List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player2, exiledCards);

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 16);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(exiledCards);
    }

    @Test
    void freeCastChoiceWithEmptyLibraryDoesNotOfferACardOrDealDamage() {
        harness.setLibrary(player2, List.of());

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void freeCastChoiceExilesAllLandsWhenThereIsNoNonlandCard() {
        List<Forest> lands = List.of(new Forest(), new Forest());
        harness.setLibrary(player2, lands);

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(lands);
        harness.assertLife(player2, 20);
    }

    @Test
    void freeCastChoiceStopsAtTheFirstNonlandCard() {
        Forest land = new Forest();
        TheWarDoctor spell = new TheWarDoctor();
        Forest remainingCard = new Forest();
        harness.setLibrary(player2, List.of(land, spell, remainingCard));

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land, spell);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageChoiceWithEmptyLibraryDealsNoDamage() {
        harness.setLibrary(player2, List.of());

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void damageChoiceUsesOnlyTheCardsAvailableInAShortLibrary() {
        Forest land = new Forest();
        TheWarDoctor spell = new TheWarDoctor();
        harness.setLibrary(player2, List.of(land, spell));

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 16);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land, spell);
    }

    @Test
    void damageChoiceExilesOnlyFourCardsAsOneEvent() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheWarDoctor());
        List<Forest> exiledCards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        Forest remainingCard = new Forest();
        harness.setLibrary(player2, List.of(exiledCards.get(0), exiledCards.get(1),
                exiledCards.get(2), exiledCards.get(3), remainingCard));

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.DAMAGE_OPTION);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(exiledCards);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void freeCastChoiceExilesCardsOneAtATime() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheWarDoctor());
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        TheWarDoctor spell = new TheWarDoctor();
        harness.setLibrary(player2, List.of(firstLand, secondLand, spell));

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstLand, secondLand, spell);
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    private void cast() {
        harness.setHand(player1, List.of(new EnsnaredByTheMara()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
