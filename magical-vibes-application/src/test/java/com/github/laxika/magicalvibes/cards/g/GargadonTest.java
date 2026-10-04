package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gargadon.class, PithingNeedle.class})
class GargadonTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Gargadon with four time counters")
    void suspendExilesWithFourTimeCounters() {
        Gargadon card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        Gargadon card = suspendCard();
        removeTimeCounter();
        removeTimeCounter();
        removeTimeCounter();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Gargadon");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Gargadon in exile")
    void decliningCastLeavesCardInExile() {
        Gargadon card = suspendCard();
        removeTimeCounter();
        removeTimeCounter();
        removeTimeCounter();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(card);
    }

    @Test
    @DisplayName("Only the owner's upkeep removes a suspend counter, after its trigger resolves")
    void upkeepCounterRemovalUsesStackAndOwnersTurn() {
        Gargadon card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Without flash, Gargadon cannot be suspended during upkeep")
    void cannotSuspendDuringUpkeep() {
        Gargadon card = new Gargadon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Casting Gargadon normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.setHand(player1, List.of(new Gargadon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Gargadon"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Pithing Needle cannot prevent the special action of suspending Gargadon")
    void pithingNeedleDoesNotPreventSuspend() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gargadon");

        Gargadon card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }
    private Gargadon suspendCard() {
        Gargadon card = new Gargadon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void removeTimeCounter() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
