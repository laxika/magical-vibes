package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unicycle.class, GrizzlyBears.class})
class UnicycleTest extends BaseCardTest {

    @Test
    void equippedCreatureHasFirstStrikeAndHaste() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent unicycle = addUnicycleReady(player1);
        unicycle.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void equipAttachesToTargetCreature() {
        Permanent unicycle = addUnicycleReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(unicycle.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void crewAnimatesUnicycleAndTapsCrewUntilEndOfTurn() {
        Permanent unicycle = addUnicycleReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unicycle)).isTrue();
        assertThat(gqs.hasKeyword(gd, unicycle, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unicycle, Keyword.HASTE)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unicycle)).isFalse();
    }

    private Permanent addUnicycleReady(Player player) {
        return addCreatureReady(player, new Unicycle());
    }

    @Test
    void crewingAttachedUnicycleDetachesItPermanently() {
        Permanent unicycle = addUnicycleReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(unicycle.getAttachedTo()).isEqualTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unicycle)).isTrue();
        assertThat(unicycle.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unicycle)).isFalse();
        assertThat(unicycle.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void summoningSickCreatureCanPayCrewCost() {
        Permanent unicycle = harness.addToBattlefieldAndReturn(player1, new Unicycle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, unicycle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, unicycle)).isTrue();
        assertThat(gqs.hasKeyword(gd, unicycle, Keyword.HASTE)).isTrue();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        Permanent unicycle = addUnicycleReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, unicycle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent unicycle = addUnicycleReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(unicycle.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
