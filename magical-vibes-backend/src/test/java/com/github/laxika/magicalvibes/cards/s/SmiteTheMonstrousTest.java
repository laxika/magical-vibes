package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmiteTheMonstrousTest extends BaseCardTest {

    @Test
    @DisplayName("Smite the Monstrous has correct card properties")
    void hasCorrectProperties() {
        SmiteTheMonstrous card = new SmiteTheMonstrous();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                )),
                "Target must be a creature with power 4 or greater"
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    @Test
    @DisplayName("Casting Smite the Monstrous targeting a creature with power 4+ puts it on stack")
    void castingPutsOnStack() {
        Permanent wurm = new Permanent(new CrawWurm());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Smite the Monstrous");
        assertThat(entry.getTargetId()).isEqualTo(wurm.getId());
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        // Add a valid target so spell is playable
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new CrawWurm()));

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a creature with exactly power 3")
    void cannotTargetPower3Creature() {
        // Add a valid target so spell is playable
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new CrawWurm()));

        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Resolving Smite the Monstrous destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent wurm = new Permanent(new CrawWurm());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Craw Wurm"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Craw Wurm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Smite the Monstrous"));
    }

    @Test
    @DisplayName("Smite the Monstrous allows regeneration")
    void allowsRegeneration() {
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Craw Wurm"));
    }

    @Test
    @DisplayName("Smite the Monstrous fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent wurm = new Permanent(new CrawWurm());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(wurm);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Smite the Monstrous"));
    }
}
