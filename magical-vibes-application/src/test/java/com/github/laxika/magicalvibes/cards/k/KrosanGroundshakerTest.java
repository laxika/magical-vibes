package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ContestedCliffs;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.r.RavenousBaloth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanGroundshaker.class, RavenousBaloth.class, ElvishWarrior.class, ContestedCliffs.class})
class KrosanGroundshakerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to a target Beast creature")
    void grantsTrampleToBeast() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can target a Beast creature controlled by an opponent")
    void grantsTrampleToOpponentsBeast() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player2, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Beast creature")
    void cannotTargetNonBeastCreature() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent nonBeast = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonBeast.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Beast creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ContestedCliffs());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Beast creature");
    }
}
