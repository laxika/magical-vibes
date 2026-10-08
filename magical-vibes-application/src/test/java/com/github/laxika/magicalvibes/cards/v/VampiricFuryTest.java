package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampiricFury.class, VampireInterloper.class, WalkingCorpse.class})
class VampiricFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Vampiric Fury gives Vampire creatures +2/+0 and first strike")
    void buffsVampireCreatures() {
        Permanent vampire = addCreatureReady(player1, new VampireInterloper()); // 2/1 Vampire
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(vampire.getEffectivePower()).isEqualTo(4);  // 2 + 2
        assertThat(vampire.getEffectiveToughness()).isEqualTo(1);  // unchanged
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Vampiric Fury does not affect non-Vampire creatures")
    void doesNotBuffNonVampires() {
        Permanent bear = addCreatureReady(player1, new WalkingCorpse()); // 2/2 Zombie
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vampiric Fury does not affect opponent's Vampires")
    void doesNotBuffOpponentVampires() {
        Permanent ownVampire = addCreatureReady(player1, new VampireInterloper());
        Permanent opponentVampire = addCreatureReady(player2, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownVampire.getEffectivePower()).isEqualTo(4);
        assertThat(ownVampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        assertThat(opponentVampire.getEffectivePower()).isEqualTo(2);
        assertThat(opponentVampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vampiric Fury effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent vampire = addCreatureReady(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(vampire.getEffectivePower()).isEqualTo(4);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getEffectivePower()).isEqualTo(2);
        assertThat(vampire.getEffectiveToughness()).isEqualTo(1);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Casting Vampiric Fury puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vampiric Fury");
    }

    @Test
    @DisplayName("All Vampires present at resolution are affected")
    void affectsMultipleVampiresAtResolution() {
        Permanent first = addCreatureReady(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        Permanent second = harness.enterBattlefieldAndReturn(player1, new VampireInterloper());

        harness.passBothPriorities();

        for (Permanent vampire : List.of(first, second)) {
            assertThat(vampire.getEffectivePower()).isEqualTo(4);
            assertThat(vampire.getEffectiveToughness()).isEqualTo(1);
            assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("Vampires entering after resolution receive neither effect")
    void doesNotAffectVampiresEnteringLater() {
        Permanent original = addCreatureReady(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent later = harness.enterBattlefieldAndReturn(player1, new VampireInterloper());

        assertThat(original.getEffectivePower()).isEqualTo(4);
        assertThat(original.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(1);
        assertThat(later.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vampiric Fury resolves without any creatures")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vampiric Fury");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
