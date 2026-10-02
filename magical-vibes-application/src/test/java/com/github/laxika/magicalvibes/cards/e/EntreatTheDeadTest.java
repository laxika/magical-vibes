package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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

@CardUsed({EntreatTheDead.class, GrizzlyBears.class, HolyDay.class})
class EntreatTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Returns X target creature cards from your graveyard")
    void returnsXTargetCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EntreatTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(first.getId()) || card.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("X=0 resolves without targets")
    void zeroTargets() {
        harness.setHand(player1, List.of(new EntreatTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Entreat the Dead");
    }

    @Test
    @DisplayName("Miracle cast uses {X}{B}{B} and returns X creatures")
    void miracleCastReturnsCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        EntreatTheDead entreat = new EntreatTheDead();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(entreat));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires exactly X legal creature targets from your graveyard")
    void requiresExactlyXLegalTargets() {
        Card creature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, instant));
        harness.setHand(player1, List.of(new EntreatTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(creature.getId(), instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
