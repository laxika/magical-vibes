package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MogissFavor.class, GrizzlyBears.class, FountainOfYouth.class})
class MogissFavorTest extends BaseCardTest {

    @Test
    void castingPutsOnStack() {
        Permanent bears = addReadyBear();
        harness.setHand(player1, List.of(new MogissFavor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    void enchantedCreatureGetsBoost() {
        Permanent bears = addReadyBear();
        Permanent aura = new Permanent(new MogissFavor());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    void effectsStopWhenRemoved() {
        Permanent bears = addReadyBear();
        Permanent aura = new Permanent(new MogissFavor());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void fizzlesIfTargetRemoved() {
        Permanent bears = addReadyBear();
        harness.setHand(player1, List.of(new MogissFavor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MogissFavor);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof MogissFavor);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MogissFavor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void escapeExilesTwoOtherCardsAndAttachesAura() {
        Permanent bears = addReadyBear();
        MogissFavor aura = new MogissFavor();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(aura, first, second));
        harness.addMana(player1, ManaColor.BLACK, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, bears.getId(), List.of(), List.of(1, 2), null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == aura
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    void escapeRequiresTwoOtherCardsInGraveyard() {
        Permanent bears = addReadyBear();
        harness.setGraveyard(player1, List.of(new MogissFavor(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, bears.getId(), List.of(), List.of(0), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBear() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        return bears;
    }
}
