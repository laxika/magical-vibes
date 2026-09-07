package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.c.ClayStatue;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XenicPoltergeist.class, Millstone.class, ClayStatue.class})
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
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
    }

    @Test
    @DisplayName("Can animate a noncreature artifact controlled by an opponent")
    void animatesOpponentsArtifact() {
        addCreatureReady(player1, new XenicPoltergeist());
        harness.addToBattlefield(player2, new Millstone());

        Permanent millstone = findPermanent(player2, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        millstone = findPermanent(player2, "Millstone");
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addCreatureReady(player1, new XenicPoltergeist());
        harness.addToBattlefield(player2, new ClayStatue()); // artifact creature

        Permanent target = findPermanent(player2, "Clay Statue");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Animation lasts through the controller's next untap step and ends at the next upkeep")
    void animationLastsUntilNextUpkeep() {
        addCreatureReady(player1, new XenicPoltergeist());
        harness.addToBattlefield(player1, new Millstone());

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Millstone"))).isTrue();

        // Stop during player1's untap step to verify that the animation has not expired too early.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        millstone = findPermanent(player1, "Millstone");
        assertThat(gqs.isCreature(gd, millstone)).isTrue();

        advanceToUpkeep(player1);

        millstone = findPermanent(player1, "Millstone");
        assertThat(gqs.isCreature(gd, millstone)).isFalse();
    }
}
