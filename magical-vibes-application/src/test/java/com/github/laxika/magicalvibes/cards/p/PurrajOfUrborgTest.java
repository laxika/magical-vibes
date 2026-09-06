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

import java.util.List;

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

    private void giveBlackSpell(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new FeralShadow()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
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
        giveBlackSpell(player1);
        harness.addMana(player1, ManaColor.BLACK, 1); // the {B} to pay

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, purraj)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the payment leaves Purraj unchanged")
    void decliningLeavesPurrajUnchanged() {
        Permanent purraj = addPurraj();
        giveBlackSpell(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
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
        giveBlackSpell(player1);

        harness.castCreature(player1, 0);

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
        giveBlackSpell(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gqs.getEffectivePower(gd, purraj)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, purraj)).isEqualTo(4);
    }

    @Test
    @DisplayName("A nonblack spell does not trigger the ability")
    void nonBlackSpellDoesNotTrigger() {
        Permanent purraj = addPurraj();
        harness.setHand(player1, List.of(new GibberingHyenas()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
