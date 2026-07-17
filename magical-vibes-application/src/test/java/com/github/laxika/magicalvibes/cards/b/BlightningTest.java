package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlightningTest extends BaseCardTest {

    // "Blightning deals 3 damage to target player or planeswalker. That player or that
    //  planeswalker's controller discards two cards."

    private void giveBlightning() {
        harness.setHand(player1, List.of(new Blightning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    // ===== Target player =====

    @Test
    @DisplayName("Deals 3 damage to the targeted player and makes them discard two cards")
    void damageAndDiscardToTargetPlayer() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        giveBlightning();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);

        // The targeted player (not the caster) discards two cards of their choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty-handed target still takes 3 damage with no discard")
    void emptyHandTargetStillTakesDamage() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        giveBlightning();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Target planeswalker =====

    @Test
    @DisplayName("Targeting a planeswalker removes 3 loyalty and its controller discards two cards")
    void damageAndDiscardToPlaneswalkerController() {
        ElspethKnightErrant elspethCard = new ElspethKnightErrant();
        Permanent elspeth = new Permanent(elspethCard);
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        giveBlightning();

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3

        // The planeswalker's controller discards, not the caster.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveBlightning();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
