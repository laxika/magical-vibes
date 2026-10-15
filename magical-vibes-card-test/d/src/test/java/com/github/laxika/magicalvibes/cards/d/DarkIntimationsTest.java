package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkIntimations.class, Forest.class, GrizzlyBears.class, LilianaVess.class,
        NicolBolasGodPharaoh.class, Shock.class})
class DarkIntimationsTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices, discards, and the controller returns a creature then draws")
    void resolvesAllSpellEffects() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Shock())));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new DarkIntimations()));
        addDarkIntimationsMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A Bolas planeswalker enters with an additional loyalty counter and exiles Dark Intimations")
    void triggersForBolasPlaneswalkerSpells() {
        DarkIntimations darkIntimations = new DarkIntimations();
        harness.setGraveyard(player1, List.of(darkIntimations));
        harness.setHand(player1, List.of(new NicolBolasGodPharaoh()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        Permanent bolas = findPermanent(player1, "Nicol Bolas, God-Pharaoh");
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(darkIntimations.getId()));
        harness.assertNotInGraveyard(player1, "Dark Intimations");
    }

    @Test
    @DisplayName("The graveyard ability does not trigger for a non-Bolas planeswalker")
    void doesNotTriggerForNonBolasPlaneswalkerSpells() {
        harness.setGraveyard(player1, List.of(new DarkIntimations()));
        harness.setHand(player1, List.of(new LilianaVess()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dark Intimations");
    }

    @Test
    void cannotDeclineReturningAnEligibleCard() {
        harness.setHand(player1, List.of(new DarkIntimations()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(new NicolBolasGodPharaoh()));
        harness.setLibrary(player1, List.of(new Shock()));
        addDarkIntimationsMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertInHand(player1, "Nicol Bolas, God-Pharaoh");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void opponentCanChoosePlaneswalkerInsteadOfCreature() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DarkIntimations(), new Shock()));
        harness.setGraveyard(player1, List.of(new NicolBolasGodPharaoh(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        addDarkIntimationsMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player2, List.of(bolas.getId()));
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Nicol Bolas, God-Pharaoh");
        harness.assertInGraveyard(player2, "Nicol Bolas, God-Pharaoh");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Nicol Bolas, God-Pharaoh");
        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void stillDiscardsAndDrawsWhenThereIsNothingToSacrificeOrReturn() {
        harness.setHand(player1, List.of(new DarkIntimations()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        addDarkIntimationsMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void eachGraveyardCopyGrantsAnAdditionalLoyaltyCounter() {
        DarkIntimations first = new DarkIntimations();
        DarkIntimations second = new DarkIntimations();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new NicolBolasGodPharaoh()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Nicol Bolas, God-Pharaoh")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(9);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        harness.assertNotInGraveyard(player1, "Dark Intimations");
    }

    @Test
    void opponentsBolasDoesNotTriggerTheGraveyardAbility() {
        harness.setGraveyard(player1, List.of(new DarkIntimations()));
        harness.setHand(player2, List.of(new NicolBolasGodPharaoh()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Nicol Bolas, God-Pharaoh")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Dark Intimations");
    }

    private void addDarkIntimationsMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
