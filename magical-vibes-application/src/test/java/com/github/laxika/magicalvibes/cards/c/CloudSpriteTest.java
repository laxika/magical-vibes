package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudSpriteTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Cloud Sprite has correct card properties")
    void hasCorrectProperties() {
        CloudSprite card = new CloudSprite();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CanBlockOnlyIfAttackerMatchesPredicateEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Cloud Sprite puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cloud Sprite");
    }

    @Test
    @DisplayName("Resolving puts Cloud Sprite onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cloud Sprite"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new CloudSprite()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cloud Sprite enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cloud Sprite"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Blocking — can block creatures with flying =====

    @Test
    @DisplayName("Cloud Sprite can block a creature with flying")
    void canBlockFlyingCreature() {
        // Player2 has Cloud Sprite as potential blocker
        Permanent spritePerm = new Permanent(new CloudSprite());
        spritePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spritePerm);

        // Player1 has a flying attacker (Air Elemental)
        Permanent atkPerm = new Permanent(new AirElemental());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Should not throw — Cloud Sprite can block flyers
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spritePerm.isBlocking()).isTrue();
    }

    // ===== Blocking — cannot block creatures without flying =====

    @Test
    @DisplayName("Cloud Sprite cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        // Player2 has Cloud Sprite as potential blocker
        Permanent spritePerm = new Permanent(new CloudSprite());
        spritePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spritePerm);

        // Player1 has a ground attacker (Grizzly Bears)
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    // ===== Combat — Cloud Sprite trades with another 1/1 flyer =====

    @Test
    @DisplayName("Cloud Sprite trades in combat with another 1/1 flyer")
    void tradesWithOneOneFlyer() {
        harness.setLife(player2, 20);

        // Player1 has Cloud Sprite as attacker
        Permanent atkPerm = new Permanent(new CloudSprite());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        // Player2 has Cloud Sprite as blocker
        Permanent blockerPerm = new Permanent(new CloudSprite());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Both should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cloud Sprite"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cloud Sprite"));

        // No damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Cloud Sprite deals combat damage when unblocked =====

    @Test
    @DisplayName("Unblocked Cloud Sprite deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = new Permanent(new CloudSprite());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}


