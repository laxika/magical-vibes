package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurseOfTheNightlyHuntTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has MustAttackEffect with ENCHANTED_PLAYER_CREATURES scope")
    void hasCorrectEffect() {
        CurseOfTheNightlyHunt card = new CurseOfTheNightlyHunt();

        assertThat(card.isEnchantPlayer()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustAttackEffect.class);
        MustAttackEffect effect = (MustAttackEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.scope()).isEqualTo(GrantScope.ENCHANTED_PLAYER_CREATURES);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CurseOfTheNightlyHunt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Curse of the Nightly Hunt");
    }

    @Test
    @DisplayName("Resolving puts curse onto the battlefield attached to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfTheNightlyHunt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Curse of the Nightly Hunt"))
                .findFirst().orElseThrow();
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    // ===== Must attack effect =====

    @Test
    @DisplayName("Enchanted player's creatures must attack")
    void enchantedPlayerCreaturesMustAttack() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declaring no attackers should fail because bears must attack
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Enchanted player's creatures can successfully be declared as attackers")
    void enchantedPlayerCreaturesCanAttack() {
        harness.setLife(player1, 20);

        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Curse controller's creatures are NOT affected")
    void doesNotAffectControllerCreatures() {
        harness.setLife(player2, 20);

        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Controller's bears should NOT be forced to attack
        gs.declareAttackers(gd, player1, List.of());
    }

    @Test
    @DisplayName("Tapped creatures are not forced to attack")
    void tappedCreaturesNotForced() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Tapped creature can't attack, so no exception
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sick creatures are not forced to attack")
    void summoningSickCreaturesNotForced() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        // Bears with summoning sickness
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // Another creature without summoning sickness that can optionally attack
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only bears2 (index 1) must attack; bears (index 0) has summoning sickness
        gs.declareAttackers(gd, player2, List.of(1));
    }

    // ===== Effect removed when curse leaves =====

    @Test
    @DisplayName("Must-attack effect is removed when curse leaves the battlefield")
    void effectRemovedWhenCurseLeaves() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // Remove curse
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == curse);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Now bears can choose not to attack
        gs.declareAttackers(gd, player2, List.of());
    }

    // ===== Multiple creatures =====

    @Test
    @DisplayName("All of enchanted player's creatures must attack")
    void allCreaturesMustAttack() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        harness.addToBattlefield(player1, curse);
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == curse)
                .findFirst().orElseThrow();
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only declaring one creature should fail — both must attack
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
