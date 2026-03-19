package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TendrilsOfCorruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Tendrils of Corruption has correct effect configuration")
    void hasCorrectEffect() {
        TendrilsOfCorruption card = new TendrilsOfCorruption();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class);
        DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                (DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.SWAMP);
        assertThat(effect.gainLife()).isTrue();
    }

    @Test
    @DisplayName("Casting Tendrils of Corruption targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Tendrils of Corruption");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Tendrils deals damage to creature equal to Swamps controlled and gains life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 3 damage kills Grizzly Bears (2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Controller gains 3 life (equal to Swamp count)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Tendrils counts only controller's Swamps, not opponent's")
    void countsOnlyControllerSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Only 1 damage (1 Swamp controlled by player1), Grizzly Bears survives (2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Controller gains 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Tendrils counts Swamps at resolution time")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove all Swamps before resolution
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Swamp"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 0 Swamps at resolution, so 0 damage and 0 life gain
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tendrils does not count non-Swamp lands")
    void doesNotCountNonSwampLands() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Only 1 Swamp, so 1 damage (Grizzly Bears survives) and 1 life gained
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }
}
