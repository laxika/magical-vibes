package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LifelinkTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Lifelink has correct card properties")
    void hasCorrectProperties() {
        Lifelink card = new Lifelink();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.keyword()).isEqualTo(Keyword.LIFELINK);
        assertThat(effect.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Lifelink puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lifelink");
    }

    @Test
    @DisplayName("Resolving Lifelink attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lifelink")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Grants lifelink keyword =====

    @Test
    @DisplayName("Enchanted creature has lifelink")
    void enchantedCreatureHasLifelink() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent lifelinkPerm = new Permanent(new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(lifelinkPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();
    }

    // ===== Lifelink gains life on combat damage =====

    @Test
    @DisplayName("Enchanted creature gains controller life equal to combat damage dealt")
    void gainsLifeOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent lifelinkPerm = new Permanent(new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(lifelinkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Player1 gains 2 life (bears power), player2 loses 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses lifelink when Lifelink aura is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent lifelinkPerm = new Permanent(new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(lifelinkPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(lifelinkPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isFalse();
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Lifelink does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent lifelinkPerm = new Permanent(new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(lifelinkPerm);

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.LIFELINK)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Lifelink fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lifelink"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Lifelink"));
    }
}
