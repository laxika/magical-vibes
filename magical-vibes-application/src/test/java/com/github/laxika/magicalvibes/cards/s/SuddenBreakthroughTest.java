package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenBreakthrough.class, ScurridColony.class, LetterOfAcceptance.class})
class SuddenBreakthroughTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature, grants first strike, and creates a Treasure")
    void boostsCreatureAndCreatesTreasure() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        harness.setHand(player1, List.of(new SuddenBreakthrough()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    @DisplayName("Pump and first strike wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        harness.setHand(player1, List.of(new SuddenBreakthrough()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new SuddenBreakthrough()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Letter of Acceptance");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting an opponent's creature gives the Treasure to the caster")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        harness.setHand(player1, List.of(new SuddenBreakthrough()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates no Treasure when the only target leaves before resolution")
    void illegalTargetPreventsTreasureCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        harness.setHand(player1, List.of(new SuddenBreakthrough()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SuddenBreakthrough);
    }
}
