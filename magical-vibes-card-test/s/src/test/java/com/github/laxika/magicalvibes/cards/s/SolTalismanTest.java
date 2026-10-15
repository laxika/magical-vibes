package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MeddlingMage;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolTalisman.class, PithingNeedle.class, MeddlingMage.class})
class SolTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Sol Talisman with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        SolTalisman card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        SolTalisman card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sol Talisman");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Tapping Sol Talisman adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new SolTalisman());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pithing Needle does not prevent suspending Sol Talisman")
    void namedAbilityRestrictionDoesNotPreventSuspend() {
        harness.addToBattlefieldAndReturn(player2, new PithingNeedle()).setChosenName("Sol Talisman");

        SolTalisman card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Sol Talisman exiled without another offer")
    void decliningCastLeavesCardExiled() {
        SolTalisman card = suspendCard();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Sol Talisman");
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove Sol Talisman's time counters")
    void opponentUpkeepDoesNotRemoveCounters() {
        SolTalisman card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Suspending Sol Talisman requires paying one mana")
    void cannotSuspendWithoutMana() {
        SolTalisman card = new SolTalisman();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    @DisplayName("Meddling Mage naming Sol Talisman prevents suspending it")
    void castingProhibitionPreventsSuspend() {
        harness.addToBattlefieldAndReturn(player2, new MeddlingMage()).setChosenName("Sol Talisman");
        SolTalisman card = new SolTalisman();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private SolTalisman suspendCard() {
        SolTalisman card = new SolTalisman();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
