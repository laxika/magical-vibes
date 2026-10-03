package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.e.EnchantmentAlteration;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BartelRuneaxe.class, GiantStrength.class, BarbaryApes.class, ChainLightning.class,
        EnchantmentAlteration.class, Replenish.class})
class BartelRuneaxeTest extends BaseCardTest {

    @Test
    void vigilanceKeepsBartelUntappedWhenAttacking() {
        Permanent bartel = addCreatureReady(player1, new BartelRuneaxe());
        declareAttackers(List.of(0));
        assertThat(bartel.isTapped()).isFalse();
        assertThat(bartel.isAttacking()).isTrue();
    }

    @Test
    void ownAuraSpellsCannotTargetBartel() {
        Permanent bartel = harness.addToBattlefieldAndReturn(player1, new BartelRuneaxe());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bartel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be enchanted by other Auras");
    }

    @Test
    void auraCanMoveOntoBartelWithoutBeingCast() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        Permanent bartel = harness.addToBattlefieldAndReturn(player2, new BartelRuneaxe());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, ape.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Giant Strength");

        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(bartel.getId());
        assertThat(gqs.getEffectivePower(gd, bartel)).isEqualTo(8);
    }

    @Test
    void auraReturningFromGraveyardCanEnchantBartel() {
        Permanent bartel = harness.addToBattlefieldAndReturn(player2, new BartelRuneaxe());
        GiantStrength aura = new GiantStrength();
        harness.setGraveyard(player1, List.of(aura));

        harness.castFromHand(player1, new Replenish(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Strength");
        assertThat(findPermanent(player1, "Giant Strength").getAttachedTo()).isEqualTo(bartel.getId());
        harness.assertNotInGraveyard(player1, "Giant Strength");
        assertThat(gqs.getEffectivePower(gd, bartel)).isEqualTo(8);
    }


    @Test
    @DisplayName("Aura spells cannot target Bartel Runeaxe")
    void auraSpellsCannotTargetBartelRuneaxe() {
        Permanent bartel = harness.addToBattlefieldAndReturn(player2, new BartelRuneaxe());

        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bartel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be enchanted by other Auras");
    }

    @Test
    @DisplayName("Non-Aura spells can target Bartel Runeaxe")
    void nonAuraSpellsCanTargetBartelRuneaxe() {
        Permanent bartel = harness.addToBattlefieldAndReturn(player2, new BartelRuneaxe());

        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, bartel.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Chain Lightning"));
    }

    @Test
    @DisplayName("Aura spells can target other creatures")
    void auraSpellsCanTargetOtherCreatures() {
        Permanent ape = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, ape.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(4);
    }
}
