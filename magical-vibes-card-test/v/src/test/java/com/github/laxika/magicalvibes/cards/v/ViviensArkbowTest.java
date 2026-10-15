package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViviensArkbow.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class ViviensArkbowTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature with mana value at most X from the top X cards onto the battlefield")
    void putsEligibleCreatureOntoBattlefield() {
        Card discarded = new Forest();
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant tooExpensive = new HillGiant();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(eligible, tooExpensive));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts all looked-at cards on the bottom when no creature qualifies")
    void noEligibleCreatureLeavesCardsOnBottom() {
        Card discarded = new Forest();
        HillGiant creatureOverLimit = new HillGiant();
        Forest land = new Forest();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(creatureOverLimit, land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof HillGiant);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creatureOverLimit, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @DisplayName("May decline an eligible creature and bottoms only the looked-at cards")
    void mayDeclineEligibleCreature() {
        Card discarded = new Forest();
        GrizzlyBears eligible = new GrizzlyBears();
        Forest lookedAtLand = new Forest();
        HillGiant untouched = new HillGiant();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(eligible, lookedAtLand, untouched));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(eligible, lookedAtLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X zero still taps the Arkbow and discards a card without changing the library")
    void zeroStillPaysNonManaCosts() {
        Card discarded = new Forest();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(untouched));

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library does not reduce the chosen X used as the mana-value limit")
    void shortLibraryKeepsChosenManaValueLimit() {
        HillGiant eligible = new HillGiant();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(eligible));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot put two eligible creatures onto the battlefield")
    void cannotChooseMoreThanOneCreature() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest untouched = new Forest();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard()).isSameAs(second));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still allows activation and discarding a creature as its cost")
    void emptyLibraryStillPaysCosts() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
