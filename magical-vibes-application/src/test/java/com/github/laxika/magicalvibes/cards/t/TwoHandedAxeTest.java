package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SweepingCleave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHandedAxe.class, SweepingCleave.class, GrizzlyBears.class})
class TwoHandedAxeTest extends BaseCardTest {

    @Test
    void equippedCreatureHasItsPowerDoubledWhenItAttacks() {
        Permanent creature = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        creature.setPowerModifier(1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void adventureGrantsDoubleStrikeToCreatureYouControlAndExilesTheCard() {
        TwoHandedAxe card = new TwoHandedAxe();
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetOpponentCreature() {
        TwoHandedAxe card = new TwoHandedAxe();
        Permanent creature = addCreatureReady(player2);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addAxeReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new TwoHandedAxe());
    }

}
