package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GibberingHyenas;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurrajOfUrborg.class, FeralShadow.class, GibberingHyenas.class})
class PurrajOfUrborgTest extends BaseCardTest {

    private Permanent addPurraj() {
        Permanent purraj = addCreatureReady(player1, new PurrajOfUrborg());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return purraj;
    }

    @Test
    @DisplayName("Has first strike only while attacking")
    void firstStrikeOnlyWhileAttacking() {
        Permanent purraj = addPurraj();

        assertThat(gqs.hasKeyword(gd, purraj, Keyword.FIRST_STRIKE)).isFalse();

        purraj.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, purraj, Keyword.FIRST_STRIKE)).isTrue();

        purraj.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, purraj, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Controller casts a black spell, pays {B}, gets a +1/+1 counter")
    void controllerCastsBlackSpellAndPays() {
        Permanent purraj = addPurraj();
        harness.addMana(player1, ManaColor.BLACK, 1); // the {B} to pay

        harness.castFromHand(player1, new FeralShadow(), "{2}{B}");
        harness.passBothPriorities(); // resolve triggered ability to its payment choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, purraj)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the payment leaves Purraj unchanged")
    void decliningLeavesPurrajUnchanged() {
        Permanent purraj = addPurraj();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromHand(player1, new FeralShadow(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, purraj)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting without {B} leaves Purraj unchanged")
    void acceptingWithoutPaymentLeavesPurrajUnchanged() {
        Permanent purraj = addPurraj();
        harness.castFromHand(player1, new FeralShadow(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, purraj)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's black spell also triggers the ability")
    void opponentBlackSpellTriggers() {
        Permanent purraj = addPurraj();
        harness.addMana(player1, ManaColor.BLACK, 1); // controller's {B} to pay

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FeralShadow(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, purraj)).isEqualTo(4);
    }

    @Test
    @DisplayName("A nonblack spell does not trigger the ability")
    void nonBlackSpellDoesNotTrigger() {
        addPurraj();
        harness.castFromHand(player1, new GibberingHyenas(), "{2}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The trigger goes on the stack before its payment choice")
    void paymentChoiceWaitsForResolution() {
        Permanent purraj = addPurraj();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromHand(player1, new FeralShadow(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Purraj does not gain first strike while blocking")
    void noFirstStrikeWhileBlocking() {
        Permanent purraj = addPurraj();
        purraj.setBlocking(true);

        assertThat(gqs.hasKeyword(gd, purraj, Keyword.FIRST_STRIKE)).isFalse();
    }
}
