package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
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

class ChandrasDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a red creature and does not loot")
    void dealsDamageToRedCreatureNoLoot() {
        Permanent redCreature = new Permanent(new HillGiant()); // 3/3 red
        redCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(redCreature);

        Card keeper = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new ChandrasDefeat(), keeper)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, redCreature.getId());
        harness.passBothPriorities();

        // Hill Giant (3/3) took 5 damage and died.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(redCreature.getId()));

        // Not a Chandra planeswalker → no loot: no prompt, hand unchanged, nothing drawn.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a Chandra planeswalker, then loots on accept")
    void dealsDamageToChandraAndLoots() {
        // Chandra, Bold Pyromancer is a red Chandra planeswalker.
        Permanent chandra = new Permanent(new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 10);
        chandra.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(chandra);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        Card toDiscard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new ChandrasDefeat(), toDiscard)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        // 5 damage removed 5 loyalty.
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        // Chandra planeswalker → may discard a card, and if you do, draw a card.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Discard happens before the draw (rummage).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Declining the loot on a Chandra planeswalker discards and draws nothing")
    void chandraLootDecline() {
        Permanent chandra = new Permanent(new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 10);
        chandra.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(chandra);

        Card keeper = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new ChandrasDefeat(), keeper)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        // No discard, no draw.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-red creature")
    void cannotTargetNonRedCreature() {
        // A legal red target exists, so the spell is castable; the green creature is an illegal choice.
        Permanent red = new Permanent(new HillGiant()); // red 3/3
        red.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(red);
        Permanent green = new Permanent(new GrizzlyBears()); // green 2/2
        green.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(green);

        harness.setHand(player1, new ArrayList<>(List.of(new ChandrasDefeat())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, green.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }
}
