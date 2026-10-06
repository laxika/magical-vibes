package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({RestlessVinestalk.class, Gingerbrute.class})
class RestlessVinestalkTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Vinestalk enters tapped and produces green or blue mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessVinestalk()));
        harness.playLand(player1, 0);

        Permanent vinestalk = findPermanent(player1, "Restless Vinestalk");
        assertThat(vinestalk.isTapped()).isTrue();

        vinestalk.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Restless Vinestalk becomes a 5/5 blue and green Plant with trample and stays a land")
    void animatesIntoPlant() {
        Permanent vinestalk = addReadyVinestalk(player1);
        animateVinestalk(player1);

        assertThat(gqs.isCreature(gd, vinestalk)).isTrue();
        assertThat(gqs.isLand(gd, vinestalk)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vinestalk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vinestalk)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, vinestalk))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, vinestalk)).contains(CardSubtype.PLANT);
        assertThat(gqs.hasKeyword(gd, vinestalk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Restless Vinestalk's animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent vinestalk = addReadyVinestalk(player1);
        animateVinestalk(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vinestalk)).isFalse();
        assertThat(gqs.isLand(gd, vinestalk)).isTrue();
    }

    @Test
    @DisplayName("Attacking with Restless Vinestalk sets another creature's base power and toughness to 3/3")
    void attackingSetsOtherCreatureBasePowerAndToughness() {
        Permanent vinestalk = addReadyVinestalk(player1);
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opposingCreature = addReadyCreature(player2);
        animateVinestalk(player1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vinestalk)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId(), opposingCreature.getId())
                .doesNotContain(vinestalk.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Restless Vinestalk's attack trigger can resolve without a target")
    void attackingCanResolveWithoutTarget() {
        Permanent vinestalk = addReadyVinestalk(player1);
        animateVinestalk(player1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vinestalk)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Restless Vinestalk can produce green mana without using the stack")
    void producesGreenMana() {
        Permanent vinestalk = addReadyVinestalk(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(vinestalk.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger may decline an available creature target")
    void canDeclineAvailableTarget() {
        Permanent vinestalk = addReadyVinestalk(player1);
        Permanent creature = addReadyCreature(player1);
        animateVinestalk(player1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vinestalk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vinestalk)).isEqualTo(5);
    }

    @Test
    @DisplayName("The attack trigger can affect an opposing creature and preserves its counters until cleanup")
    void opposingTargetKeepsCountersAndBaseChangeExpires() {
        addReadyVinestalk(player1);
        Permanent creature = addReadyCreature(player2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        animateVinestalk(player1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger can target another animated Restless Vinestalk")
    void canTargetAnotherVinestalk() {
        Permanent attacker = addReadyVinestalk(player1);
        Permanent target = addReadyVinestalk(player2);
        animateVinestalk(player1);
        animateVinestalk(player2);

        declareAttackers(List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(attacker.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
    }

    private void animateVinestalk(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyVinestalk(Player player) {
        return addCreatureReady(player, new RestlessVinestalk());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new Gingerbrute());
    }
}
