package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SquadronCarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetworkMarauder.class, MyrRetriever.class, SolRing.class, SquadronCarrier.class, GrizzlyBears.class})
class NetworkMarauderTest extends BaseCardTest {

    @Test
    void entersAndPerpetuallyBoostsOwnedArtifactCreaturesAndSpacecraft() {
        Permanent myr = addOwnedPermanent(player1, new MyrRetriever());
        Permanent spacecraft = addOwnedPermanent(player1, new SquadronCarrier());
        Permanent bears = addOwnedPermanent(player1, new GrizzlyBears());
        int myrPower = gqs.getEffectivePower(gd, myr);
        int myrToughness = gqs.getEffectiveToughness(gd, myr);
        int spacecraftPower = gqs.getEffectivePower(gd, spacecraft);
        int spacecraftToughness = gqs.getEffectiveToughness(gd, spacecraft);

        harness.setHand(player1, List.of(new NetworkMarauder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent marauder = findPermanent(player1, "Network Marauder");
        assertThat(gqs.getEffectivePower(gd, marauder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marauder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(myrPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(myrToughness + 1);
        assertThat(gqs.getEffectivePower(gd, spacecraft)).isEqualTo(spacecraftPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, spacecraft)).isEqualTo(spacecraftToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void onlyAnotherArtifactWithManaValueAtLeastThreeTriggersTheAbility() {
        Permanent myr = addOwnedPermanent(player1, new MyrRetriever());
        harness.setHand(player1, List.of(new NetworkMarauder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int powerAfterFirstTrigger = gqs.getEffectivePower(gd, myr);
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(powerAfterFirstTrigger);

        harness.setHand(player1, List.of(new NetworkMarauder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(powerAfterFirstTrigger + 2);
    }

    @Test
    void doesNotBoostOrdinaryArtifactCreatureTokens() {
        MyrRetriever tokenCopy = new MyrRetriever();
        tokenCopy.setToken(true);
        Permanent token = addOwnedPermanent(player1, tokenCopy);

        harness.enterBattlefieldAndReturn(player1, new NetworkMarauder());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void boostsOwnedCardsInHiddenZonesGraveyardAndExile() {
        NetworkMarauder handCard = new NetworkMarauder();
        NetworkMarauder libraryCard = new NetworkMarauder();
        NetworkMarauder graveyardCard = new NetworkMarauder();
        NetworkMarauder exiledCard = new NetworkMarauder();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));

        harness.enterBattlefieldAndReturn(player1, new NetworkMarauder());
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        gd.exiledCards.removeIf(exiled -> exiled.card().getId().equals(exiledCard.getId()));
        for (NetworkMarauder card : List.of(handCard, libraryCard, graveyardCard, exiledCard)) {
            Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
        }
    }

    @Test
    void boostsOwnedCardsUnderOpposingControlButNotBorrowedCards() {
        NetworkMarauder ownedCard = new NetworkMarauder();
        ownedCard.setOwnerId(player1.getId());
        Permanent owned = harness.addToBattlefieldAndReturn(player2, ownedCard);
        NetworkMarauder borrowedCard = new NetworkMarauder();
        borrowedCard.setOwnerId(player2.getId());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, borrowedCard);

        harness.enterBattlefieldAndReturn(player1, new NetworkMarauder());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, owned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owned)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, borrowed)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, borrowed)).isEqualTo(2);
    }

    @Test
    void opposingArtifactEnteringDoesNotTriggerTheAbility() {
        Permanent marauder = addOwnedPermanent(player1, new NetworkMarauder());

        harness.enterBattlefieldAndReturn(player2, new SquadronCarrier());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, marauder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, marauder)).isEqualTo(2);
    }

    @Test
    void warpTriggersTheBoostAndExilesTheMarauderAtEndStep() {
        Permanent myr = addOwnedPermanent(player1, new MyrRetriever());
        NetworkMarauder card = new NetworkMarauder();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(2);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Network Marauder")).isZero();
        assertThat(gd.exiledCards).anySatisfy(exiled ->
                assertThat(exiled.card().getId()).isEqualTo(card.getId()));
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(2);
    }

    private Permanent addOwnedPermanent(com.github.laxika.magicalvibes.model.Player player,
                                        com.github.laxika.magicalvibes.model.Card card) {
        card.setOwnerId(player.getId());
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
