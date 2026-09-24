package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreamingFury.class, Arachnoid.class, ConjurersBauble.class})
class ScreamingFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +5/+0 and haste until end of turn")
    void givesBoostAndHaste() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new ScreamingFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(5);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Boost and haste wear off at cleanup")
    void boostAndHasteWearOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new ScreamingFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        harness.setHand(player1, List.of(new ScreamingFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(5);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
        harness.setHand(player1, List.of(new ScreamingFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bauble.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new ScreamingFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Screaming Fury");
    }
}
