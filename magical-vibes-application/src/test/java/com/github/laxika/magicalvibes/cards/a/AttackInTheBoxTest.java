package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AttackInTheBox.class)
class AttackInTheBoxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the optional boost")
    void attackingOffersBoost() {
        Permanent box = addCreatureReady(player1, new AttackInTheBox());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, box)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, box)).isEqualTo(4);
    }

    @Test
    @DisplayName("Accepting the boost sacrifices the creature at the next end step")
    void acceptingBoostSacrificesAtNextEndStep() {
        Permanent box = addCreatureReady(player1, new AttackInTheBox());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(box.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Attack-in-the-Box"));
    }

    @Test
    @DisplayName("Declining the boost leaves the creature alive and unmodified")
    void decliningBoostDoesNothing() {
        Permanent box = addCreatureReady(player1, new AttackInTheBox());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, box)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, box)).isEqualTo(4);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(box.getId()));
    }
}
