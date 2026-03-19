package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TravelPreparationsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has one SPELL effect: put +1/+1 counter on target creature")
    void hasCorrectEffects() {
        TravelPreparations card = new TravelPreparations();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);

        PutPlusOnePlusOneCounterOnTargetCreatureEffect effect =
                (PutPlusOnePlusOneCounterOnTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has flashback cost {1}{W}")
    void hasFlashbackCost() {
        TravelPreparations card = new TravelPreparations();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{1}{W}");
    }

    // ===== Casting normally — single target =====

    @Test
    @DisplayName("Casting on one creature puts a +1/+1 counter on it")
    void singleTargetGetsCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Casting normally — two targets =====

    @Test
    @DisplayName("Casting on two creatures puts a +1/+1 counter on each")
    void twoTargetsEachGetCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castSorcery(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf.get(0).getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(bf.get(1).getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Can target opponent's creatures =====

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Goes to graveyard after resolving =====

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Travel Preparations"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback puts +1/+1 counter on target creature")
    void flashbackPutsCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback on two targets puts +1/+1 counter on each")
    void flashbackTwoTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castFlashback(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf.get(0).getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(bf.get(1).getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Travel Preparations"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Travel Preparations"));
    }

    @Test
    @DisplayName("Flashback puts sorcery spell on stack")
    void flashbackPutsOnStackAsSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, List.of(bearId));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Travel Preparations");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        // Only 1 white mana, but flashback costs {1}{W}
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
