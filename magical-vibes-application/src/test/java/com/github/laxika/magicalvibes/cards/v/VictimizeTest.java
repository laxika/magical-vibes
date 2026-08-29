package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VictimizeTest extends BaseCardTest {

    @Test
    @DisplayName("Requires exactly two target creature cards")
    void requiresExactlyTwoCreatureTargets() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(first, second));
        castVictimize();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("Sacrifices a creature and returns both chosen creatures tapped")
    void sacrificesAndReturnsChosenCreaturesTapped() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(first, second));
        castVictimize();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(first.getId())
                        || permanent.getCard().getId().equals(second.getId()))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("Returns the surviving target if the other target left the graveyard")
    void returnsSurvivingTargetAfterOtherLeavesGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(first, second));
        castVictimize();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(first);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()) && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sacrifice.getCard().getId()));
    }

    @Test
    @DisplayName("Does not sacrifice when both targets are illegal")
    void doesNotSacrificeWhenBothTargetsAreIllegal() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(first, second));
        castVictimize();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("Returns nothing when no creature can be sacrificed")
    void returnsNothingWhenNoCreatureCanBeSacrificed() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        castVictimize();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot cast with fewer than two creature cards in the graveyard")
    void cannotCastWithFewerThanTwoCreatureCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HolyDay()));
        harness.setHand(player1, List.of(new Victimize()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    private void castVictimize() {
        harness.setHand(player1, List.of(new Victimize()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
    }
}
