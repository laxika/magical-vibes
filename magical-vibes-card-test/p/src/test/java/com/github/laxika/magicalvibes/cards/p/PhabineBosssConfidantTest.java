package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhabineBosssConfidant.class, Forest.class, GrizzlyBears.class})
class PhabineBosssConfidantTest extends BaseCardTest {

    @Test
    @DisplayName("Parley creates Citizens for lands, boosts for nonlands, and draws the revealed cards")
    void parleyCreatesCitizensBoostsAndDraws() {
        Permanent phabine = addCreatureReady(player1, new PhabineBosssConfidant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card ownLand = new Forest();
        Card opponentNonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opponentNonland));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent citizen = findPermanents(player1, "Citizen").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, phabine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phabine)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, citizen, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(ownLand);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentNonland);
    }

    @Test
    @DisplayName("Parley's temporary boost wears off at the end of the turn")
    void parleyBoostExpiresAtEndOfTurn() {
        Permanent phabine = addCreatureReady(player1, new PhabineBosssConfidant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, phabine)).isEqualTo(4);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.getEffectivePower(gd, phabine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, phabine)).isEqualTo(6);
    }

    @Test
    @DisplayName("Parley does not create a Citizen when no land is revealed")
    void parleyCreatesNoCitizenWithoutLands() {
        Permanent phabine = addCreatureReady(player1, new PhabineBosssConfidant());
        Card ownNonland = new GrizzlyBears();
        Card opponentNonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownNonland));
        harness.setLibrary(player2, List.of(opponentNonland));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, phabine)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, phabine)).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownNonland);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentNonland);
    }

    @Test
    @DisplayName("Two revealed lands create two unboosted Citizens and grant haste only while Phabine remains")
    void allLandsCreateTwoCitizensWithContinuousHaste() {
        Permanent phabine = addCreatureReady(player1, new PhabineBosssConfidant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        List<Permanent> citizens = findPermanents(player1, "Citizen");
        assertThat(citizens).hasSize(2);
        assertThat(findPermanents(player2, "Citizen")).isEmpty();
        for (Permanent citizen : citizens) {
            assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, citizen, Keyword.HASTE)).isTrue();
        }
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, phabine, Keyword.HASTE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(phabine);
        for (Permanent citizen : citizens) {
            assertThat(gqs.hasKeyword(gd, citizen, Keyword.HASTE)).isFalse();
        }
    }

    @Test
    @DisplayName("Parley does not trigger during an opponent's combat")
    void opponentCombatDoesNotTriggerParley() {
        addCreatureReady(player1, new PhabineBosssConfidant());
        Card ownLand = new Forest();
        Card opponentLand = new Forest();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opponentLand));

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    @DisplayName("Creatures entering after Parley resolves do not receive its temporary boost")
    void laterCreaturesDoNotReceiveParleyBoost() {
        addCreatureReady(player1, new PhabineBosssConfidant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent laterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
