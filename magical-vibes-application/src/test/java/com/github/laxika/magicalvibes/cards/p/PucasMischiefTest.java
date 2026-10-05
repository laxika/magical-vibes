package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.c.Cursecatcher;
import com.github.laxika.magicalvibes.cards.d.DeepchannelMentor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PucasMischief.class, BriarberryCohort.class, Cursecatcher.class,
        DeepchannelMentor.class, Forest.class, GreaterAuramancy.class})
class PucasMischiefTest extends BaseCardTest {

    @Test
    @DisplayName("Does not offer your own permanent with shroud as the first target")
    void excludesOwnPermanentWithShroud() {
        Permanent puca = harness.addToBattlefieldAndReturn(player1, new PucasMischief());
        Permanent auramancy = harness.addToBattlefieldAndReturn(player1, new GreaterAuramancy());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        harness.addToBattlefield(player2, new Cursecatcher());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(auramancy.getId(), own.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(puca.getId());
    }

    @Test
    @DisplayName("Does not offer an opponent permanent with shroud as the second target")
    void excludesOpponentPermanentWithShroud() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DeepchannelMentor());
        Permanent opponentPuca = harness.addToBattlefieldAndReturn(player2, new PucasMischief());
        Permanent auramancy = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(auramancy.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentPuca.getId());
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new PucasMischief());
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player2, new Cursecatcher());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Briarberry Cohort");
        harness.assertOnBattlefield(player2, "Cursecatcher");
    }

    @Test
    @DisplayName("Exchanges control of both permanents when accepted")
    void exchangesControlWhenAccepted() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort()); // MV 2
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new Cursecatcher());      // MV 1

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());  // nonland permanent you control
        harness.handlePermanentChosen(player1, opp.getId());  // opponent's permanent (MV <= own)
        harness.passBothPriorities();                         // resolve trigger to the "may" prompt
        harness.handleMayAbilityChosen(player1, true);        // accept the exchange

        harness.assertOnBattlefield(player2, "Briarberry Cohort");
        harness.assertNotOnBattlefield(player1, "Briarberry Cohort");
        harness.assertOnBattlefield(player1, "Cursecatcher");
        harness.assertNotOnBattlefield(player2, "Cursecatcher");
    }

    @Test
    @DisplayName("No exchange when the controller declines the may ability")
    void noExchangeWhenDeclined() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new Cursecatcher());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);       // decline

        harness.assertOnBattlefield(player1, "Briarberry Cohort");
        harness.assertOnBattlefield(player2, "Cursecatcher");
    }

    @Test
    @DisplayName("Does not trigger a choice when no legal target pair exists")
    void noLegalPairDoesNothing() {
        // Player1's nonland permanents (Puca's Mischief MV 4, Briarberry Cohort MV 2) are all below
        // the only opponent permanent's mana value (Deepchannel Mentor MV 6), so no legal pair can
        // be chosen.
        harness.addToBattlefield(player1, new PucasMischief());
        harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        harness.addToBattlefieldAndReturn(player2, new DeepchannelMentor());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Briarberry Cohort");
        harness.assertOnBattlefield(player2, "Deepchannel Mentor");
    }

    @Test
    @DisplayName("Exchange fizzles when a target leaves the battlefield before resolution")
    void exchangeFizzlesWhenTargetGone() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new Cursecatcher());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opp.getId());
        // Opponent's target leaves the battlefield before the ability resolves.
        gd.playerBattlefields.get(player2.getId()).remove(opp);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // No exchange happened — the controller keeps their permanent.
        harness.assertOnBattlefield(player1, "Briarberry Cohort");
        harness.assertNotOnBattlefield(player2, "Briarberry Cohort");
    }

    @Test
    @DisplayName("Allows an exchange with an opponent permanent of equal mana value")
    void exchangesEqualManaValuePermanents() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new BriarberryCohort());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opp.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(own.getId());
    }

    @Test
    @DisplayName("Only offers nonland opponent permanents as the second target")
    void onlyOffersNonlandOpponentPermanents() {
        harness.addToBattlefield(player1, new PucasMischief());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Cursecatcher());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, own.getId());

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.validPermanentIds()).containsExactly(opponent.getId());
        assertThat(opponentChoice.validPermanentIds()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(land.getId(), own.getId());
    }

    @Test
    @DisplayName("Can exchange control of Puca's Mischief itself")
    void canExchangeSourcePermanent() {
        Permanent puca = harness.addToBattlefieldAndReturn(player1, new PucasMischief());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Cursecatcher());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, puca.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(puca.getId());
    }
}
