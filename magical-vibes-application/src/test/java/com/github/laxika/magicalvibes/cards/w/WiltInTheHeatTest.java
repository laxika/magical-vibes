package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WiltInTheHeatTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has cost reduction + mark-exile + 5 damage effects")
    void cardStructure() {
        WiltInTheHeat card = new WiltInTheHeat();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anySatisfy(e -> {
                    assertThat(e).isInstanceOf(ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect.class);
                    assertThat(((ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect) e).amount()).isEqualTo(2);
                });
        var spell = card.getEffects(EffectSlot.SPELL);
        assertThat(spell).hasSize(2);
        assertThat(spell.get(0)).isInstanceOf(MarkTargetCreatureExileInsteadOfDieThisTurnEffect.class);
        assertThat(spell.get(1)).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(((DealDamageToTargetCreatureEffect) spell.get(1)).damage()).isEqualTo(new Fixed(5));
    }

    // ===== Damage + exile instead of die =====

    @Test
    @DisplayName("Kills the creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2); // 2 generic (no cost reduction)

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Exiled, not in the graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Marks a surviving creature so a later death this turn exiles it")
    void marksSurvivorForExile() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Avatar (8/8) survives 5 damage but is flagged to be exiled if it dies this turn
        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Costs {2} less when a card left the graveyard this turn")
    void costReducedWhenCardLeftGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        // Only {R}{W} available — enough only if reduced by {2}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be cast with only {R}{W} when no card left the graveyard")
    void notCastableWithoutReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
