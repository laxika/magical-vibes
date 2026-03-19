package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssaultStrobeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Assault Strobe has correct card properties")
    void hasCorrectProperties() {
        AssaultStrobe card = new AssaultStrobe();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.keyword()).isEqualTo(Keyword.DOUBLE_STRIKE);
        assertThat(effect.scope()).isEqualTo(GrantScope.TARGET);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Assault Strobe puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssaultStrobe()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Assault Strobe");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Assault Strobe grants double strike to target creature")
    void resolvingGrantsDoubleStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssaultStrobe()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Assault Strobe goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssaultStrobe()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Assault Strobe"));
    }

    // ===== Double strike in combat =====

    @Test
    @DisplayName("Creature with double strike deals damage to player in both combat phases")
    void doubleStrikeDealsDoublePlayerDamage() {
        harness.setLife(player2, 20);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getGrantedKeywords().add(Keyword.DOUBLE_STRIKE);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Grizzly Bears (2/2) with double strike deals 2 + 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Double strike keyword is removed at end of turn")
    void doubleStrikeRemovedAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.getGrantedKeywords().add(Keyword.DOUBLE_STRIKE);

        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Assault Strobe fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssaultStrobe()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Assault Strobe"));
    }
}
