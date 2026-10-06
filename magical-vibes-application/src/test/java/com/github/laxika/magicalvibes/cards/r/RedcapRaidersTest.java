package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
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

@CardUsed({RedcapRaiders.class, VenerableKnight.class, Gingerbrute.class})
class RedcapRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a non-Human creature boosts Redcap Raiders and grants trample")
    void tappingNonHumanCreatureBoostsAndGrantsTrample() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent golem = addCreatureReady(player1, new Gingerbrute());
        addCreatureReady(player1, new Gingerbrute());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, golem.getId());

        assertThat(golem.isTapped()).isTrue();
        assertThat(raiders.getEffectivePower()).isEqualTo(4);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A Human creature cannot be tapped for Redcap Raiders")
    void humanCreatureCannotBeTapped() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent human = addCreatureReady(player1, new VenerableKnight());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(human.isTapped()).isFalse();
        assertThat(raiders.getEffectivePower()).isEqualTo(3);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Declining the tap leaves Redcap Raiders unchanged")
    void decliningTapLeavesRaidersUnchanged() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent golem = addCreatureReady(player1, new Gingerbrute());
        addCreatureReady(player1, new Gingerbrute());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(golem.isTapped()).isFalse();
        assertThat(raiders.getEffectivePower()).isEqualTo(3);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Redcap Raiders loses the boost and trample at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent golem = addCreatureReady(player1, new Gingerbrute());
        addCreatureReady(player1, new Gingerbrute());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, golem.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raiders.getEffectivePower()).isEqualTo(3);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick non-Human can pay the attack trigger's tap cost")
    void summoningSickCreatureCanPayTapCost() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent helper = addCreatureReady(player1, new RedcapRaiders());
        helper.setSummoningSick(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(helper.isTapped()).isTrue();
        assertThat(helper.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, helper, Keyword.TRAMPLE)).isFalse();
        assertThat(raiders.getEffectivePower()).isEqualTo(4);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An already-tapped creature cannot pay the attack trigger's tap cost")
    void tappedCreatureCannotPayTapCost() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent helper = addCreatureReady(player1, new RedcapRaiders());
        helper.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(helper.isTapped()).isTrue();
        assertThat(raiders.getEffectivePower()).isEqualTo(3);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's non-Human creature cannot pay the tap cost")
    void opponentsCreatureCannotPayTapCost() {
        Permanent raiders = addCreatureReady(player1, new RedcapRaiders());
        Permanent opponentCreature = addCreatureReady(player2, new RedcapRaiders());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(raiders.getEffectivePower()).isEqualTo(3);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raiders, Keyword.TRAMPLE)).isFalse();
    }
}
