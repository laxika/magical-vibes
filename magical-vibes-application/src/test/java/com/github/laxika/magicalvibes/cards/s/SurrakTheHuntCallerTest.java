package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.cards.g.GladeWatcher;
import com.github.laxika.magicalvibes.cards.w.WanderingTombshell;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrakTheHuntCaller.class, ColossodonYearling.class, GladeWatcher.class,
        Flatten.class, WanderingTombshell.class})
class SurrakTheHuntCallerTest extends BaseCardTest {

    @Test
    @DisplayName("Formidable does not trigger below total power eight")
    void doesNotTriggerBelowTotalPowerEight() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        addCreatureReady(player1, new ColossodonYearling());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Formidable lets Surrak grant haste to a creature you control")
    void grantsHasteAtTotalPowerEight() {
        Permanent surrak = addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new GladeWatcher());
        Permanent opponentCreature = addCreatureReady(player2, new GladeWatcher());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(surrak.getId(), target.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Surrak's granted haste wears off at end of turn")
    void grantedHasteWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new GladeWatcher());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to formidable")
    void opponentPowerDoesNotCount() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player2, new GladeWatcher());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Surrak does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new GladeWatcher());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A newly entered Surrak can grant itself haste and attack")
    void canGrantItselfHasteAndAttack() {
        Permanent surrak = harness.addToBattlefieldAndReturn(player1, new SurrakTheHuntCaller());
        addCreatureReady(player1, new GladeWatcher());
        assertThat(surrak.isSummoningSick()).isTrue();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, surrak.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, surrak, Keyword.HASTE)).isTrue();
        declareAttackers(List.of(0));
        assertThat(surrak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Formidable is checked again when the trigger resolves")
    void doesNotGrantHasteIfPowerFallsBelowEight() {
        Permanent surrak = addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent contributor = addCreatureReady(player1, new GladeWatcher());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, surrak.getId());
        harness.castAndResolveInstant(player1, 0, contributor.getId());
        harness.assertInGraveyard(player1, "Glade Watcher");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, surrak, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves after Surrak leaves if total power remains eight or more")
    void resolvesWithoutSourceWhenPowerRemainsHighEnough() {
        Permanent surrak = addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new GladeWatcher());
        addCreatureReady(player1, new GladeWatcher());
        addCreatureReady(player1, new GladeWatcher());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, surrak.getId());
        harness.assertInGraveyard(player1, "Surrak, the Hunt Caller");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Negative creature power reduces the total for formidable")
    void negativePowerCountsTowardTotal() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        addCreatureReady(player1, new GladeWatcher());
        addCreatureReady(player1, new ColossodonYearling());
        Permanent tombshell = addCreatureReady(player1, new WanderingTombshell());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, tombshell.getId());
        assertThat(gqs.getEffectivePower(gd, tombshell)).isEqualTo(-3);

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("A removed target does not transfer haste to another creature")
    void removedTargetDoesNotGrantHasteElsewhere() {
        Permanent surrak = addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new GladeWatcher());
        Permanent other = addCreatureReady(player1, new GladeWatcher());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, surrak, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
