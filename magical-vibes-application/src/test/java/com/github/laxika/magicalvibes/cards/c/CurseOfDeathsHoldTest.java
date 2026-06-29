package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfDeathsHoldTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has static boost effect targeting enchanted player's creatures")
    void hasCorrectEffect() {
        CurseOfDeathsHold card = new CurseOfDeathsHold();

        assertThat(card.isEnchantPlayer()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(-1);
        assertThat(boost.toughnessBoost()).isEqualTo(-1);
        assertThat(boost.scope()).isEqualTo(GrantScope.ENCHANTED_PLAYER_CREATURES);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Curse of Death's Hold");
    }

    @Test
    @DisplayName("Resolving puts curse onto the battlefield attached to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Curse of Death's Hold"))
                .findFirst().orElseThrow();
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    // ===== Static effect: debuffs enchanted player's creatures =====

    @Test
    @DisplayName("Enchanted player's creatures get -1/-1")
    void debuffsEnchantedPlayerCreatures() {
        CurseOfDeathsHold curse = new CurseOfDeathsHold();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // 2/2 base - 1/1 from curse = 1/1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Curse controller's own creatures are NOT affected")
    void doesNotDebuffControllerCreatures() {
        CurseOfDeathsHold curse = new CurseOfDeathsHold();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Controller's creature is unaffected: 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Effect applies when cast and resolved =====

    @Test
    @DisplayName("Debuff applies when curse resolves onto battlefield")
    void debuffAppliesOnResolve() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Before casting, no debuff
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        // After resolving, creature debuffed
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    // ===== Debuff removed when curse leaves =====

    @Test
    @DisplayName("Debuff is removed when curse leaves the battlefield")
    void debuffRemovedWhenCurseLeaves() {
        CurseOfDeathsHold curse = new CurseOfDeathsHold();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        // Remove curse
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == curse);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Multiple curses stack =====

    @Test
    @DisplayName("Two curses give -2/-2 to enchanted player's creatures")
    void twoCursesStack() {
        CurseOfDeathsHold curse1 = new CurseOfDeathsHold();
        CurseOfDeathsHold curse2 = new CurseOfDeathsHold();
        harness.addToBattlefield(player1, curse1);
        harness.addToBattlefield(player1, curse2);

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CurseOfDeathsHold)
                .forEach(p -> p.setAttachedTo(player2.getId()));

        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // 2/2 base - 2/2 from two curses = 0/0
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }
}
