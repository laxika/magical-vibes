package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralDeceiver.class, HumbleBudoka.class, Forest.class})
class FeralDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability looks at the top card and leaves it on top")
    void looksAtTopCard() {
        addReadyDeceiver(player1);
        Card topCard = new HumbleBudoka();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Looking at an empty library does not create a card-choice interaction")
    void lookingAtEmptyLibraryDoesNotPrompt() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The look ability can be activated more than once each turn")
    void lookAbilityCanBeActivatedMoreThanOnceEachTurn() {
        addReadyDeceiver(player1);
        Card topCard = new HumbleBudoka();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Revealing a land gives +2/+2 and trample until end of turn")
    void landRevealBoostsAndGrantsTrample() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Revealing an empty library grants nothing")
    void emptyLibraryRevealGrantsNothing() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(2);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Revealing a nonland card grants nothing")
    void nonlandRevealGrantsNothing() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new HumbleBudoka()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The reveal ability can only be activated once each turn")
    void revealAbilityOnlyOnceEachTurn() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The reveal ability can be activated again on a new turn")
    void revealAbilityCanBeActivatedAgainOnNewTurn() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(5);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The reveal ability checks the top card at resolution rather than activation")
    void checksTopCardAtResolution() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new HumbleBudoka()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A land present at activation does not grant a bonus if the top card changes to a nonland")
    void landAtActivationDoesNotGuaranteeBonus() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        Card nonland = new HumbleBudoka();
        harness.setLibrary(player1, List.of(nonland));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(2);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Both abilities can be used while tapped and summoning sick in the same turn")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent deceiver = addReadyDeceiver(player1);
        deceiver.setSummoningSick(true);
        deceiver.tap();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(deceiver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The reveal activation limit applies while the first activation is still on the stack")
    void cannotActivateRevealAgainBeforeResolution() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Revealing a nonland still uses up the reveal activation for the turn")
    void nonlandRevealUsesActivationLimit() {
        Permanent deceiver = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new HumbleBudoka()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(2);
        assertThat(deceiver.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Each Deceiver has its own reveal activation limit and boosts only itself")
    void separateDeceiversHaveIndependentLimits() {
        Permanent first = addReadyDeceiver(player1);
        Permanent second = addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyDeceiver(Player player) {
        return addCreatureReady(player, new FeralDeceiver());
    }
}
