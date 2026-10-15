package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChiefOfTheEdge;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArashinForemost.class, ChiefOfTheEdge.class, DromokaWarrior.class,
        ColossodonYearling.class, Flatten.class})
class ArashinForemostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield grants another Warrior you control double strike")
    void enteringGrantsDoubleStrike() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());

        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking grants another Warrior you control double strike")
    void attackingGrantsDoubleStrike() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());
        addCreatureReady(player1, new ArashinForemost());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The trigger can target only another Warrior you control")
    void targetMustBeAnotherWarriorYouControl() {
        Permanent ownWarrior = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new ChiefOfTheEdge());

        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownWarrior.getId());
        assertThat(choice.validIds()).doesNotContain(opponentWarrior.getId());
    }

    @Test
    @DisplayName("The granted double strike expires at the end of the turn")
    void doubleStrikeExpiresAtEndOfTurn() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        harness.handlePermanentChosen(player1, warrior.getId());
        resolveAllTriggers();
        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Entering without another controlled Warrior does not require a target choice")
    void enteringWithoutLegalTarget() {
        harness.addToBattlefield(player1, new ColossodonYearling());
        harness.addToBattlefield(player2, new DromokaWarrior());

        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack targeting excludes non-Warriors, opponents, and the source")
    void attackTargetRestrictions() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        harness.addToBattlefield(player1, new ColossodonYearling());
        harness.addToBattlefield(player2, new DromokaWarrior());
        addCreatureReady(player1, new ArashinForemost());

        declareAttackers(player1, List.of(2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(warrior.getId());
        harness.handlePermanentChosen(player1, warrior.getId());
        resolveAllTriggers();
        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The trigger resolves even if Arashin Foremost dies in response")
    void triggerSurvivesSourceRemoval() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        Permanent foremost = harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, foremost.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Arashin Foremost");
        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing the chosen Warrior does not grant double strike to another Warrior")
    void removedTargetDoesNotRedirectGrant() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(other.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Arashin Foremost deals combat damage in both damage steps without another Warrior")
    void sourceDealsDoubleStrikeDamageWithoutLegalTriggerTarget() {
        addCreatureReady(player1, new ArashinForemost());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The chosen Warrior deals combat damage in both damage steps")
    void grantedDoubleStrikeDealsDamageTwice() {
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        harness.handlePermanentChosen(player1, warrior.getId());
        resolveAllTriggers();

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }
}
