package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SeascapeAerialist.class, StoneworkPuma.class, RiverBoa.class, Conspiracy.class, Dominate.class})
class SeascapeAerialistTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may give flying to all Allies you control")
    void ownAllyEntryMayGrantFlyingToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new RiverBoa());
        harness.castFromHand(player1, new SeascapeAerialist(), "{4}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = findPermanent(player1, "Seascape Aerialist");
        assertThat(existingAlly.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(enteringAlly.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonAlly.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Another Ally entering may give flying to all Allies you control")
    void anotherAllyEntryMayGrantFlyingToAllAllies() {
        Permanent existingAerialist = harness.addToBattlefieldAndReturn(player1, new SeascapeAerialist());
        harness.castFromHand(player1, new SeascapeAerialist(), "{4}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAerialist = findPermanents(player1, "Seascape Aerialist").get(1);
        assertThat(existingAerialist.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(enteringAerialist.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the triggered ability grants no flying")
    void mayBeDeclined() {
        harness.castFromHand(player1, new SeascapeAerialist(), "{4}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Seascape Aerialist").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent aerialist = harness.addToBattlefieldAndReturn(player1, new SeascapeAerialist());
        harness.castFromHand(player1, new RiverBoa(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.castFromHand(player1, new SeascapeAerialist(), "{4}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        Permanent aerialist = findPermanent(player1, "Seascape Aerialist");
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Its own entry triggers even when Conspiracy replaces its Ally type")
    void ownEntryTriggersWithoutAllySubtype() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new SeascapeAerialist(), "{4}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(findPermanent(player1, "Seascape Aerialist").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger the Aerialist")
    void opponentAllyEntryDoesNotTrigger() {
        Permanent aerialist = harness.addToBattlefieldAndReturn(player1, new SeascapeAerialist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new StoneworkPuma(), "{3}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(findPermanent(player2, "Stonework Puma").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying excludes opposing Allies and does not affect later Allies")
    void flyingOnlyAffectsControlledAlliesPresentAtResolution() {
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.castFromHand(player1, new SeascapeAerialist(), "{4}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Seascape Aerialist").hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(opposingAlly.hasKeyword(Keyword.FLYING)).isFalse();

        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        resolveAllTriggers();
        Permanent laterAlly = findPermanent(player1, "Stonework Puma");
        assertThat(laterAlly.hasKeyword(Keyword.FLYING)).isFalse();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(laterAlly.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(findPermanent(player1, "Seascape Aerialist").hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A stolen Aerialist is excluded when its original controller's trigger resolves")
    void stolenSourceDoesNotGainFlyingFromOriginalControllersTrigger() {
        Permanent aerialist = harness.addToBattlefieldAndReturn(player1, new SeascapeAerialist());
        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        Permanent ally = findPermanent(player1, "Stonework Puma");

        harness.setHand(player2, List.of(new Dominate()));
        harness.addMana(player2, ManaColor.BLUE, 8);
        harness.castInstant(player2, 0, 5, aerialist.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Seascape Aerialist")).isSameAs(aerialist);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ally.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
