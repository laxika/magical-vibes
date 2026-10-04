package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalufsFinalAct.class, Assassinate.class, GrizzlyBears.class, GloriousAnthem.class})
class GalufsFinalActTest extends BaseCardTest {

    @Test
    @DisplayName("The death trigger puts counters equal to the creature's boosted power")
    void deathTriggerUsesBoostedPower() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        target.tap();
        destroyWithAssassinate(target.getId());

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The up-to-one death target may be declined")
    void deathTargetMayBeDeclined() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        target.tap();
        destroyWithAssassinate(target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger includes continuous bonuses in last-known power")
    void deathTriggerIncludesAnthemBonus() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        target.tap();
        destroyWithAssassinate(target.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("The opponent controls the ability granted to their creature and may target either side")
    void opponentControlsGrantedDeathTrigger() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent friendlyRecipient = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingRecipient = addCreatureReady(player2, new GrizzlyBears());

        castOn(target);
        target.tap();
        destroyWithAssassinate(target.getId());

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(friendlyRecipient.getId(), opposingRecipient.getId());
        harness.handlePermanentChosen(player2, friendlyRecipient.getId());
        harness.passBothPriorities();

        assertThat(friendlyRecipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposingRecipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two casts grant two independent death triggers that each use the final power")
    void multipleCastsGrantIndependentTriggers() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        castOn(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        target.tap();
        destroyWithAssassinate(target.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        resolveAllTriggers();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("The power boost and granted ability expire at the end of the turn")
    void boostAndGrantedAbilityExpire() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        target.tap();
        destroyWithAssassinate(target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger can resolve without a target when no creatures remain")
    void deathTriggerWithNoRemainingCreatures() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        target.tap();
        destroyWithAssassinate(target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GalufsFinalAct()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroyWithAssassinate(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, targetId);
    }
}
