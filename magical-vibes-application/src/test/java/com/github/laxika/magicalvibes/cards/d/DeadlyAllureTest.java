package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeadlyAllureTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Deadly Allure has correct effects")
    void hasCorrectEffects() {
        DeadlyAllure card = new DeadlyAllure();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(grant.keywords()).containsExactly(Keyword.DEATHTOUCH);
        assertThat(grant.scope()).isEqualTo(GrantScope.TARGET);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(MustBeBlockedIfAbleThisTurnEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Deadly Allure targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Deadly Allure");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving grants deathtouch and sets must-be-blocked flag")
    void resolvingGrantsDeathtouchAndMustBeBlocked() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Deathtouch and must-be-blocked flag wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    // ===== Spell goes to graveyard =====

    @Test
    @DisplayName("Deadly Allure goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deadly Allure"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deadly Allure"));
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback from graveyard grants deathtouch and must-be-blocked")
    void flashbackFromGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setGraveyard(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setGraveyard(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Deadly Allure"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deadly Allure"));
    }
}
