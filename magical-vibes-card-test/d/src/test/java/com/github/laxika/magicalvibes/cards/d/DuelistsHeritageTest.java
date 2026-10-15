package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelistsHeritage.class, GrizzlyBears.class})
class DuelistsHeritageTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever one or more creatures attack, an attacking creature may gain double strike")
    void grantsDoubleStrikeToTargetAttacker() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Multiple attackers produce one trigger that grants double strike to only one creature")
    void multipleAttackersTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(first.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(second.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own attack also triggers the ability")
    void grantsDoubleStrikeToOwnAttacker() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A target that stops attacking before resolution does not gain double strike")
    void targetMustStillBeAttackingOnResolution() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.passBothPriorities());

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional keyword grant does nothing")
    void mayDeclineGrant() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The keyword grant wears off at end of turn")
    void grantWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));
        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
