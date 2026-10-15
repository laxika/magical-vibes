package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttackInTheBox.class})
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
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

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
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(box.getId()));
    }

    @Test
    @DisplayName("The boost lasts through combat and sacrifice waits for the end-step trigger to resolve")
    void boostPersistsUntilDelayedSacrificeResolves() {
        Permanent box = addCreatureReady(player1, new AttackInTheBox());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleMayAbilityChosen(player1, true));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(box);
        assertThat(gqs.getEffectivePower(gd, box)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, box)).isEqualTo(4);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(box);
        assertThat(gqs.getEffectivePower(gd, box)).isEqualTo(6);

        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(box);
        harness.assertInGraveyard(player1, "Attack-in-the-Box");
    }

    @Test
    @DisplayName("Only the attacking Box is boosted and scheduled for sacrifice")
    void anotherBoxIsUnaffected() {
        Permanent attacker = addCreatureReady(player1, new AttackInTheBox());
        Permanent otherBox = addCreatureReady(player1, new AttackInTheBox());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherBox)).isEqualTo(2);

        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherBox).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(attacker.getCard());
    }
}
