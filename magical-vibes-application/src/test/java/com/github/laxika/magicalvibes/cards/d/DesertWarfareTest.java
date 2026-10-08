package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EternalWitness;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesertWarfare.class, HashepOasis.class, AirElemental.class,
        Millstone.class, FaithlessLooting.class, Forest.class, EternalWitness.class})
class DesertWarfareTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one hasty multicolored Sand Warrior for each Desert when you control five")
    void createsSandWarriorsForFiveDeserts() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new HashepOasis());
        }

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Sand Warrior");
        assertThat(tokens).hasSize(5);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Does not create Sand Warriors with fewer than five Deserts")
    void doesNotCreateSandWarriorsWithFourDeserts() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new HashepOasis());
        }

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Returns a sacrificed Desert under your control at your next end step")
    void returnsSacrificedDesertAtNextEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent oasis = harness.addToBattlefieldAndReturn(player1, new HashepOasis());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oasis),
                2, null, target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(oasis.getCard().getId()));

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(oasis.getCard().getId()));
    }

    @Test
    @DisplayName("Returns a Desert discarded from hand at your next end step")
    void returnsDiscardedDesertAtNextEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        harness.setHand(player1, List.of(new FaithlessLooting(), new HashepOasis(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Hashep Oasis"));
    }

    @Test
    @DisplayName("Returns a Desert milled from the library at your next end step")
    void returnsMilledDesertAtNextEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setLibrary(player1, List.of(new HashepOasis(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Hashep Oasis"));
    }

    @Test
    @DisplayName("A Desert milled on an opponent's turn waits for your end step")
    void waitsForControllersEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        HashepOasis desert = new HashepOasis();
        harness.setLibrary(player1, List.of(desert, new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hashep Oasis");
        harness.assertInGraveyard(player1, "Hashep Oasis");

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("The delayed return uses the stack and allows responses at the end step")
    void delayedReturnWaitsForPriorityPasses() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setLibrary(player1, List.of(new HashepOasis(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Hashep Oasis");
        harness.assertNotOnBattlefield(player1, "Hashep Oasis");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("A Desert that leaves and re-enters the graveyard is not the card scheduled to return")
    void doesNotReturnNewGraveyardObject() {
        Permanent warfare = harness.addToBattlefieldAndReturn(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        HashepOasis desert = new HashepOasis();
        harness.setLibrary(player1, List.of(desert, new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new EternalWitness(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(desert.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Hashep Oasis");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, warfare));

        harness.setHand(player1, List.of(new FaithlessLooting(), desert, new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hashep Oasis");
        harness.assertNotOnBattlefield(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("Sand Warriors retain their granted haste after the turn ends")
    void hasteHasNoExpiration() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new HashepOasis());
        }
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        List<Permanent> tokens = findPermanents(player1, "Sand Warrior");
        assertThat(tokens).hasSize(5);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        for (Permanent token : tokens) {
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("The combat ability does nothing if the Desert count drops below five before resolution")
    void checksDesertThresholdAgainOnResolution() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new HashepOasis());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new HashepOasis());
        }
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, desert));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
    }

    @Test
    @DisplayName("The combat ability counts your Deserts at resolution, excluding opponents' Deserts")
    void countsDesertsAtResolution() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new HashepOasis());
            harness.addToBattlefield(player2, new HashepOasis());
        }
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.addToBattlefield(player1, new HashepOasis());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(6);
        assertThat(findPermanents(player2, "Sand Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Each Desert milled together returns at your next end step")
    void returnsEveryDesertInMillBatch() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setLibrary(player1, List.of(new HashepOasis(), new HashepOasis()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hashep Oasis")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("Destroying a Desert does not trigger the sacrifice ability")
    void doesNotReturnDestroyedDesert() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new HashepOasis());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, desert));

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hashep Oasis");
        harness.assertNotOnBattlefield(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("Cards already in the graveyard when Desert Warfare enters are not returned")
    void doesNotReturnEarlierGraveyardCards() {
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        harness.castFromHand(player1, new DesertWarfare(), "{3}{G}");
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hashep Oasis");
        harness.assertNotOnBattlefield(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("The combat ability does not trigger on the opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new HashepOasis());
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
    }
}
