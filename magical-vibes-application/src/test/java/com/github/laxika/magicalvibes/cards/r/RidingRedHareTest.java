package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
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

@CardUsed({RidingRedHare.class, VolunteerMilitia.class, Forest.class})
class RidingRedHareTest extends BaseCardTest {

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new VolunteerMilitia());
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new RidingRedHare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving gives +3/+3 and horsemanship to the target creature")
    void resolvesBoostAndHorsemanship() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Boost and horsemanship wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addReadyCreature();
        castOn(target);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Riding Red Hare");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void targetsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new VolunteerMilitia());
        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castOn(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
