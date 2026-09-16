package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSledder.class, GlorySeeker.class, Forest.class})
class GoblinSledderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin gives target creature +1/+1 until end of turn")
    void sacrificingGoblinBoostsTargetCreature() {
        addCreatureReady(player1, new GoblinSledder());
        Permanent goblin = addCreatureReady(player1, new GoblinSledder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Sledder");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof GoblinSledder);
    }

    @Test
    @DisplayName("Goblin Sledder can sacrifice itself when it is the only Goblin")
    void canSacrificeItself() {
        addCreatureReady(player1, new GoblinSledder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Sledder");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Goblin Sledder's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GoblinSledder());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin Sledder cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new GoblinSledder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only a Goblin can be sacrificed to pay Goblin Sledder's cost")
    void cannotSacrificeNonGoblin() {
        Permanent sledder = addCreatureReady(player1, new GoblinSledder());
        Permanent otherSledder = addCreatureReady(player1, new GoblinSledder());
        Permanent nonGoblin = addCreatureReady(player1, new GlorySeeker());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, otherSledder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Sledder");
        harness.assertOnBattlefield(player1, "Glory Seeker");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(sledder.getId()));
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }
}
