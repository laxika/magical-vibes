package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Trollhide.class, RuneclawBear.class, Mountain.class, DoomBlade.class, Naturalize.class})
class TrollhideTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void boostsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Trollhide()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Granted ability gives the enchanted creature a regeneration shield")
    void grantedAbilityRegeneratesEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Trollhide());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost goes away when Trollhide leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Trollhide());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Trollhide()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Creature controller can regenerate a creature enchanted by an opponent")
    void opponentControlsGrantedAbility() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Trollhide()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(bear.isTapped()).isFalse();
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getRegenerationShield()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Trollhide);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof Trollhide);
    }

    @Test
    @DisplayName("Activated regeneration survives destruction of the granting Aura")
    void pendingRegenerationSurvivesAuraRemoval() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Trollhide());
        aura.setAttachedTo(bear.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getRegenerationShield()).isZero();
    }
}
