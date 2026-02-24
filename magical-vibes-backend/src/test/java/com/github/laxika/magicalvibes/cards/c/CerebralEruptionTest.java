package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CerebralEruptionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Cerebral Eruption has correct effects")
    void hasCorrectEffects() {
        CerebralEruption card = new CerebralEruption();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(RevealTopCardDealManaValueDamageEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Cerebral Eruption puts it on the stack")
    void castingPutsItOnStack() {
        setDeck(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cerebral Eruption");
    }

    // ===== Reveal and damage =====

    @Test
    @DisplayName("Deals damage equal to revealed card's mana value to target player")
    void dealsDamageToPlayer() {
        // Shock has mana value 1
        setDeck(player2, List.of(new Shock()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals damage equal to revealed card's mana value to target's creatures")
    void dealsDamageToTargetCreatures() {
        // GrizzlyBears on top has mana value 2 — deals 2 damage to each of player2's creatures
        setDeck(player2, List.of(new GrizzlyBears()));
        Permanent bear = addCreature(player2, new GrizzlyBears()); // 2/2

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Grizzly Bears (2/2) takes 2 damage = lethal, should die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player also takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not damage controller's creatures")
    void doesNotDamageControllerCreatures() {
        setDeck(player2, List.of(new GrizzlyBears())); // mana value 2
        Permanent myCreature = addCreature(player1, new GrizzlyBears()); // 2/2

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Controller's creature should be unharmed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("High mana value card deals more damage")
    void highManaValueDealsMoreDamage() {
        // CerebralEruption itself has mana value 4
        setDeck(player2, List.of(new CerebralEruption()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Land reveal: return to hand =====

    @Test
    @DisplayName("Returns to hand when a land card is revealed")
    void returnsToHandWhenLandRevealed() {
        setDeck(player2, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should be in hand, not graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cerebral Eruption"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cerebral Eruption"));
        // Land has mana value 0 — no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard when a non-land card is revealed")
    void goesToGraveyardWhenNonLandRevealed() {
        setDeck(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should be in graveyard, not hand
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cerebral Eruption"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cerebral Eruption"));
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Does nothing when target's library is empty")
    void doesNothingWhenLibraryEmpty() {
        setDeck(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CerebralEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Goes to graveyard (no land revealed)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cerebral Eruption"));
    }

    // ===== Helper methods =====

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
