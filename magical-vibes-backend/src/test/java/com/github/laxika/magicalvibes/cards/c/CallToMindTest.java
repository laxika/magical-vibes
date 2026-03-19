package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallToMindTest extends BaseCardTest {

    @Test
    @DisplayName("Call to Mind has correct effect configuration")
    void hasCorrectEffectConfiguration() {
        CallToMind card = new CallToMind();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    @Test
    @DisplayName("Call to Mind returns target instant from graveyard to hand")
    void returnsTargetInstantFromGraveyardToHand() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, instant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(instant.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(instant.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Call to Mind"));
    }

    @Test
    @DisplayName("Call to Mind returns target sorcery from graveyard to hand")
    void returnsTargetSorceryFromGraveyardToHand() {
        Card sorcery = new LavaAxe();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, sorcery.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(sorcery.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(sorcery.getId()));
    }

    @Test
    @DisplayName("Call to Mind cannot target creature card in graveyard")
    void cannotTargetCreatureCardInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Call to Mind cannot target card in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Call to Mind fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, instant.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Casting Call to Mind puts a graveyard-targeted sorcery spell on the stack")
    void castingPutsGraveyardTargetedSpellOnStack() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, instant.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(instant.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }
}
