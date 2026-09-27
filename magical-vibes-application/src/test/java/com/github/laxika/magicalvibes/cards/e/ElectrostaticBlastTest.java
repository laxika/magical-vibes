package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ElectrostaticBlast.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class ElectrostaticBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage and gives a one-time top-three play boon")
    void dealsDamageAndTriggersBoonOnNextInstantOrSorcery() {
        Card first = new Forest();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        Card fourth = new Forest();
        Card fifth = new Shock();
        Card sixth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        harness.setHand(player1, List.of(new ElectrostaticBlast(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);

        harness.handleMultipleCardsChosen(player1, List.of(third.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(third.getId(), player1.getId());
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Requires a creature, planeswalker, battle, or player target")
    void rejectsNonTargetablePermanent() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ElectrostaticBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
