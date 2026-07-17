package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XenicPoltergeistTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a noncreature artifact into an artifact creature with P/T equal to its mana value")
    void animatesArtifactWithManaValuePt() {
        addCreatureReady(player1, new XenicPoltergeist());
        harness.addToBattlefield(player1, new Millstone()); // {2} → mana value 2

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        millstone = findPermanent(player1, "Millstone");
        assertThat(millstone.isAnimatedUntilNextTurn()).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(millstone.getEffectivePower()).isEqualTo(2);
        assertThat(millstone.getEffectiveToughness()).isEqualTo(2);
        // It's still an artifact.
        assertThat(millstone.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addCreatureReady(player1, new XenicPoltergeist());
        harness.addToBattlefield(player2, new IronMyr()); // artifact creature

        Permanent target = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Animation is cleared at the beginning of the controller's next turn")
    void animationClearedAtNextTurn() {
        addCreatureReady(player1, new XenicPoltergeist());
        harness.addToBattlefield(player1, new Millstone());

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Millstone").isAnimatedUntilNextTurn()).isTrue();

        // Advance to player1's next turn — this clears the "until your next upkeep" animation.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        millstone = findPermanent(player1, "Millstone");
        assertThat(millstone.isAnimatedUntilNextTurn()).isFalse();
        assertThat(gqs.isCreature(gd, millstone)).isFalse();
    }
}
