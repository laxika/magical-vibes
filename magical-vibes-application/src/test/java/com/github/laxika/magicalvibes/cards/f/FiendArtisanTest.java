package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiendArtisan.class, Forest.class, GoldMyr.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class FiendArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each creature card in its controller's graveyard")
    void boostsForCreatureCardsInOwnGraveyard() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new Forest()));
        harness.setGraveyard(player2, List.of(new HillGiant()));

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifices another creature and searches for a creature with mana value X or less")
    void sacrificesAndSearchesWithinX() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setLibrary(player1, List.of(new LlanowarElves(), new GoldMyr(), new HillGiant(), new Forest()));

        harness.activateAbility(player1, 0, 2, null);

        assertThat(artisan.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Gold Myr");

        int goldMyrIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Gold Myr");
        harness.handleCardChosen(player1, goldMyrIndex);

        harness.assertOnBattlefield(player1, "Gold Myr");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot sacrifice itself or an opponent's creature to activate")
    void requiresAnotherControlledCreature() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player2, new FiendArtisan());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artisan.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Green pays the hybrid cost and the sacrificed card immediately increases its size")
    void greenPaymentAndImmediateGraveyardBoost() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        Permanent sacrifice = addCreatureReady(player1, new FiendArtisan());
        FiendArtisan found = new FiendArtisan();
        harness.setLibrary(player1, List.of(found, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null);

        assertThat(artisan.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(found.getId()))
                .findFirst().orElseThrow();
        assertThat(fetched.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(found);
    }

    @Test
    @DisplayName("X zero does not use the sacrificed creature's mana value as the search bound")
    void zeroXFindsNoPositiveManaValueCreature() {
        addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new FiendArtisan());
        FiendArtisan libraryCard = new FiendArtisan();
        harness.setLibrary(player1, List.of(libraryCard, new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).contains(libraryCard);
        assertThat(countPermanents(player1, "Fiend Artisan")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fiend Artisan");
    }

    @Test
    @DisplayName("May fail to find even when an eligible creature is in the library")
    void mayDeclineEligibleCreature() {
        addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new FiendArtisan());
        FiendArtisan libraryCard = new FiendArtisan();
        harness.setLibrary(player1, List.of(libraryCard, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).contains(libraryCard);
        assertThat(countPermanents(player1, "Fiend Artisan")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fiend Artisan");
    }

    @Test
    @DisplayName("The graveyard bonus updates when creature cards leave the graveyard")
    void graveyardBonusUpdates() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        harness.setGraveyard(player1, List.of(new FiendArtisan(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate outside a main phase even during its controller's turn")
    void rejectsCombatActivation() {
        addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new FiendArtisan());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate with a spell on the stack during its controller's main phase")
    void requiresEmptyStack() {
        addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new FiendArtisan());
        harness.setHand(player1, List.of(new FiendArtisan()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        artisan.setSummoningSick(true);
        addCreatureReady(player1, new FiendArtisan());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artisan.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void sorcerySpeedRestriction() {
        addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
