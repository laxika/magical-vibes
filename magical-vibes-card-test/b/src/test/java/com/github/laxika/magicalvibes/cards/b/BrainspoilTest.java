package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.cards.m.MoonlightBargain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brainspoil.class, FaithsFetters.class, GrayscaledGharial.class, LoreBroker.class,
        MoonlightBargain.class})
class BrainspoilTest extends BaseCardTest {

    @Test
    void destroysAnUnenchantedCreatureWithoutAllowingRegeneration() {
        Permanent gharial = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());
        gharial.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Brainspoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, gharial.getId());

        harness.assertNotOnBattlefield(player2, "Grayscaled Gharial");
        harness.assertInGraveyard(player2, "Grayscaled Gharial");
    }

    @Test
    void cannotTargetAnEnchantedCreature() {
        Permanent gharial = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FaithsFetters());
        aura.setAttachedTo(gharial.getId());

        harness.setHand(player1, List.of(new Brainspoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, gharial.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("isn't enchanted");
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FaithsFetters());

        harness.setHand(player1, List.of(new Brainspoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void doesNotDestroyCreatureThatBecomesEnchantedBeforeResolution() {
        Permanent gharial = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());

        harness.setHand(player1, List.of(new Brainspoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, gharial.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FaithsFetters());
        aura.setAttachedTo(gharial.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grayscaled Gharial");
        harness.assertInGraveyard(player1, "Brainspoil");
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        Brainspoil brainspoil = new Brainspoil();
        MoonlightBargain matchingCard = new MoonlightBargain();
        GrayscaledGharial differentManaValue = new GrayscaledGharial();
        harness.setHand(player1, List.of(brainspoil));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Brainspoil");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteShufflesWithoutFindingAMatchingCard() {
        Brainspoil brainspoil = new Brainspoil();
        GrayscaledGharial nonMatchingCard = new GrayscaledGharial();
        harness.setHand(player1, List.of(brainspoil));
        harness.setLibrary(player1, List.of(nonMatchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Brainspoil");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        Brainspoil brainspoil = new Brainspoil();
        harness.setHand(player1, List.of(brainspoil));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(brainspoil);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void transmuteUsesTheSourceManaValueAfterAnotherCardIsDiscardedInResponse() {
        Brainspoil brainspoil = new Brainspoil();
        MoonlightBargain matchingCard = new MoonlightBargain();
        GrayscaledGharial drawnAndDiscardedCard = new GrayscaledGharial();
        Permanent broker = addCreatureReady(player2, new LoreBroker());

        harness.setHand(player1, List.of(brainspoil));
        harness.setLibrary(player1, List.of(drawnAndDiscardedCard, matchingCard));
        harness.setHand(player2, List.of(new GrayscaledGharial()));
        harness.setLibrary(player2, List.of(new GrayscaledGharial()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(broker), null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search)
                .as("the transmute search should still use Brainspoil's mana value after a response")
                .isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Brainspoil");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmutePaysManaAndDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new Brainspoil()));
        MoonlightBargain matchingCard = new MoonlightBargain();
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Brainspoil");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Moonlight Bargain");
    }

    @Test
    void transmuteCanFailToFindEvenWhenAMatchingCardExists() {
        harness.setHand(player1, List.of(new Brainspoil()));
        MoonlightBargain matchingCard = new MoonlightBargain();
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Brainspoil");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void transmuteCannotBeActivatedDuringAnOpponentsMainPhase() {
        Brainspoil brainspoil = new Brainspoil();
        harness.setHand(player1, List.of(brainspoil));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(brainspoil);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Brainspoil");
    }
}
