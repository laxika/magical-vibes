package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimelyWard.class, GrizzlyBears.class, Plains.class})
class TimelyWardTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast at instant speed when targeting a commander")
    void canCastAtInstantSpeedWhenTargetingCommander() {
        Permanent commander = addCommander(player2);
        prepareCastOutsideSorceryTiming();

        harness.setHand(player1, List.of(new TimelyWard()));
        addWhiteAndGenericMana(player1);
        harness.castEnchantment(player1, 0, commander.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the commander flash permission for a noncommander target")
    void cannotCastAtInstantSpeedForNoncommanderTarget() {
        addCommander(player2);
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCastOutsideSorceryTiming();

        harness.setHand(player1, List.of(new TimelyWard()));
        addWhiteAndGenericMana(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ordinaryCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("Gives the enchanted creature indestructible")
    void givesEnchantedCreatureIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TimelyWard()));
        addWhiteAndGenericMana(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TimelyWard()));
        addWhiteAndGenericMana(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCommander(com.github.laxika.magicalvibes.model.Player owner) {
        Card commander = new GrizzlyBears();
        gd.makeCommander(owner.getId(), commander);
        return harness.addToBattlefieldAndReturn(owner, commander);
    }

    private void prepareCastOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    private void addWhiteAndGenericMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
    }
}
