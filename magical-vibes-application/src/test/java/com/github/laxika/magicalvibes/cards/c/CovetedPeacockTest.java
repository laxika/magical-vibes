package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetedPeacock.class, GrizzlyBears.class, Telepathy.class})
class CovetedPeacockTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger may goads a defending player's creature")
    void acceptingAttackTriggerMayGoadsTarget() {
        addCreatureReady(player1, new CovetedPeacock());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the attack trigger may does not goad the target")
    void decliningAttackTriggerMayDoesNotGoadTarget() {
        addCreatureReady(player1, new CovetedPeacock());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }

    @Test
    @DisplayName("The attack trigger only offers creatures controlled by the defending player")
    void attackTriggerFiltersTargets() {
        addCreatureReady(player1, new CovetedPeacock());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("The attack trigger does not prompt without a defending creature")
    void attackTriggerWithoutLegalTargetDoesNotPrompt() {
        addCreatureReady(player1, new CovetedPeacock());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The attack trigger cannot target a noncreature permanent")
    void attackTriggerRejectsNoncreature() {
        addCreatureReady(player1, new CovetedPeacock());
        harness.addToBattlefield(player2, new Telepathy());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNull();
    }

}
