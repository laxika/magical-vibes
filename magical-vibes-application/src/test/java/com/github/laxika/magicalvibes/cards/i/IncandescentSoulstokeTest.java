package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.action.SacrificeSelfAtNextEndStepTrigger;
import com.github.laxika.magicalvibes.cards.f.FlamekinBrawler;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncandescentSoulstoke.class, FlamekinBrawler.class, HillcomberGiant.class, Mountain.class,
        AvianChangeling.class, NamelessInversion.class})
class IncandescentSoulstokeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other Elemental creatures you control by +1/+1")
    void boostsOtherElementals() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());
        harness.addToBattlefield(player1, new FlamekinBrawler());

        Permanent elemental = findPermanent(player1, "Flamekin Brawler");

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());

        Permanent soulstoke = findPermanent(player1, "Incandescent Soulstoke");

        assertThat(gqs.getEffectivePower(gd, soulstoke)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soulstoke)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-Elemental creatures")
    void doesNotBoostNonElementals() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());
        harness.addToBattlefield(player1, new HillcomberGiant());

        Permanent giant = findPermanent(player1, "Hillcomber Giant");

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's Elemental creatures")
    void doesNotBoostOpponentElementals() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());
        harness.addToBattlefield(player2, new FlamekinBrawler());

        Permanent opponentElemental = findPermanent(player2, "Flamekin Brawler");

        assertThat(gqs.getEffectivePower(gd, opponentElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentElemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability offers only Elemental creature cards in hand")
    void abilityOffersOnlyElementalCreatures() {
        addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new Mountain(), new HillcomberGiant(), new FlamekinBrawler()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
    }

    @Test
    @DisplayName("Chosen Elemental enters with haste and is scheduled for end-step sacrifice")
    void chosenElementalEntersWithHasteAndEndStepSacrifice() {
        addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new FlamekinBrawler()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent elemental = findPermanent(player1, "Flamekin Brawler");
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(elemental.isTapped()).isFalse();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Flamekin Brawler");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Flamekin Brawler");
        harness.assertInGraveyard(player1, "Flamekin Brawler");
    }

    @Test
    @DisplayName("Declining the may leaves the Elemental in hand")
    void decliningLeavesElementalInHand() {
        addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new FlamekinBrawler()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Flamekin Brawler");
        assertThat(gd.getDelayedActions(SacrificeSelfAtNextEndStepTrigger.class)).isEmpty();
    }

    @Test
    @DisplayName("End-step activation grants haste only for that turn")
    void hasteExpiresAtCleanupAfterEndStepActivation() {
        addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new FlamekinBrawler()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        giveManaForAbility();

        putElementalFromHand(0);

        Permanent elemental = findPermanent(player1, "Flamekin Brawler");
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Flamekin Brawler");
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An Elemental put in during an end step is sacrificed at the following end step")
    void endStepActivationWaitsForOpponentsEndStep() {
        addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new FlamekinBrawler()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        giveManaForAbility();

        putElementalFromHand(0);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Flamekin Brawler");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Flamekin Brawler");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flamekin Brawler");
        harness.assertInGraveyard(player1, "Flamekin Brawler");
    }

    @Test
    @DisplayName("Changeling creatures qualify but noncreature changeling cards do not")
    void changelingCreatureQualifies() {
        addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new NamelessInversion(), new AvianChangeling()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);

        Permanent changeling = findPermanent(player1, "Avian Changeling");
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.HASTE)).isTrue();
        harness.assertInHand(player1, "Nameless Inversion");
    }

    @Test
    @DisplayName("Accepting with no eligible cards completes the ability without a choice")
    void noEligibleCardsCompletesAbility() {
        Permanent soulstoke = addCreatureReady(player1, new IncandescentSoulstoke());
        harness.setHand(player1, List.of(new HillcomberGiant(), new Mountain()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(soulstoke.isTapped()).isTrue();
        harness.assertInHand(player1, "Hillcomber Giant");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.getDelayedActions(SacrificeSelfAtNextEndStepTrigger.class)).isEmpty();
    }

    private void putElementalFromHand(int cardIndex) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, cardIndex);
    }

    private void giveManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
