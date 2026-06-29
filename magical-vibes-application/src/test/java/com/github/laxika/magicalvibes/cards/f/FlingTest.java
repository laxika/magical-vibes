package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fling has correct effects")
    void hasCorrectProperties() {
        Fling card = new Fling();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DealXDamageToAnyTargetEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Fling sacrifices a creature and stores its power in xValue")
    void castingSacrificesCreatureAndStoresPower() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Fling");
        assertThat(entry.getXValue()).isEqualTo(2); // Grizzly Bears has 2 power

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast Fling without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature for Fling")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Fling deals damage equal to sacrificed creature's power to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18); // 2 damage from Grizzly Bears' power
    }

    @Test
    @DisplayName("Fling deals damage equal to sacrificed creature's power to target creature")
    void dealsDamageToCreature() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // Sacrificed creature is gone
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Target creature should be dead (2 damage to 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fling with 1-power creature deals 1 damage")
    void onePowerCreatureDealsOneDamage() {
        harness.setLife(player2, 20);
        Permanent sacrifice = new Permanent(new RagingGoblin()); // 1/1
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Spell fizzles if target is removed before resolution — sacrifice still happens")
    void spellFizzlesIfTargetRemoved() {
        harness.setLife(player2, 20);
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent target = new Permanent(new RagingGoblin());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        // Sacrifice already happened as part of cost
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Spell fizzles — no damage dealt
        harness.assertLife(player2, 20);
    }
}
