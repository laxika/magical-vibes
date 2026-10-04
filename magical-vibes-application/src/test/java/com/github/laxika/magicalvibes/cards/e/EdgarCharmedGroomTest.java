package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FalkenrathCelebrants;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdgarCharmedGroom.class, EdgarMarkovsCoffin.class, FalkenrathCelebrants.class, HerosDownfall.class, Solemnity.class})
class EdgarCharmedGroomTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other Vampires you control +1/+1")
    void boostsOtherVampiresYouControl() {
        harness.addToBattlefieldAndReturn(player1, new EdgarCharmedGroom());
        Permanent ownVampire = harness.addToBattlefieldAndReturn(player1, new FalkenrathCelebrants());
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new FalkenrathCelebrants());

        assertThat(gqs.getEffectivePower(gd, ownVampire))
                .isEqualTo(gqs.getEffectivePower(gd, opponentVampire) + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownVampire))
                .isEqualTo(gqs.getEffectiveToughness(gd, opponentVampire) + 1);
    }

    @Test
    @DisplayName("Returns from its owner's graveyard transformed under its owner's control")
    void returnsTransformedToOwnerAfterDyingUnderAnotherPlayersControl() {
        EdgarCharmedGroom card = new EdgarCharmedGroom();
        card.setOwnerId(player1.getId());
        Permanent edgar = new Permanent(card);
        gd.playerBattlefields.get(player2.getId()).add(edgar);
        gd.stolenCreatures.put(edgar.getId(), player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, edgar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(card.getId())
                        && permanent.isTransformed());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(card.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Creates a lifelink Vampire and adds a bloodline counter at upkeep")
    void upkeepCreatesVampireAndAddsBloodlineCounter() {
        Permanent coffin = addCoffin(player1, 0);

        resolveUpkeep(player1);

        assertThat(coffin.getCounterCount(CounterType.BLOODLINE)).isEqualTo(1);
        assertThat(coffin.isTransformed()).isTrue();

        List<Permanent> tokens = vampireTokens(player1);
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Removes three bloodline counters and transforms at upkeep")
    void transformsAfterThirdBloodlineCounter() {
        Permanent coffin = addCoffin(player1, 2);

        resolveUpkeep(player1);

        assertThat(coffin.isTransformed()).isFalse();
        assertThat(coffin.getCounterCount(CounterType.BLOODLINE)).isZero();
        assertThat(vampireTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Edgar does not boost himself")
    void doesNotBoostHimself() {
        Permanent edgar = harness.addToBattlefieldAndReturn(player1, new EdgarCharmedGroom());

        assertThat(gqs.getEffectivePower(gd, edgar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, edgar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Coffin does not trigger during its opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent coffin = addCoffin(player1, 2);

        resolveUpkeep(player2);

        assertThat(coffin.getCounterCount(CounterType.BLOODLINE)).isEqualTo(2);
        assertThat(coffin.isTransformed()).isTrue();
        assertThat(vampireTokens(player1)).isEmpty();
        assertThat(vampireTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Transformation removes all bloodline counters and boosts the new Vampire")
    void transformsWithMoreThanThreeCounters() {
        Permanent coffin = addCoffin(player1, 4);

        resolveUpkeep(player1);

        assertThat(coffin.isTransformed()).isFalse();
        assertThat(coffin.getCounterCount(CounterType.BLOODLINE)).isZero();
        Permanent token = vampireTokens(player1).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Coffin still creates a Vampire when it leaves before the upkeep trigger resolves")
    void createsTokenEvenIfCoffinHasLeftBattlefield() {
        Permanent coffin = addCoffin(player1, 2);
        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(coffin);
        gd.playerGraveyards.get(player1.getId()).add(coffin.getOriginalCard());

        harness.passBothPriorities();

        assertThat(vampireTokens(player1)).hasSize(1);
        Permanent token = vampireTokens(player1).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Coffin transforms with three existing counters even when Solemnity prevents adding another")
    void transformsWithExistingCountersWhenCounterPlacementIsPrevented() {
        Permanent coffin = addCoffin(player1, 3);
        harness.addToBattlefield(player1, new Solemnity());

        resolveUpkeep(player1);

        assertThat(coffin.isTransformed()).isFalse();
        assertThat(coffin.getCounterCount(CounterType.BLOODLINE)).isZero();
        assertThat(vampireTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Edgar does not return when removed from the graveyard before its death trigger resolves")
    void doesNotReturnAfterLeavingGraveyard() {
        EdgarCharmedGroom card = new EdgarCharmedGroom();
        Permanent edgar = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, edgar.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        gd.playerGraveyards.get(player1.getId()).remove(card);
        gd.addToExile(player1.getId(), card);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(card.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    private void resolveUpkeep(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    private Permanent addCoffin(Player player, int bloodlineCounters) {
        EdgarCharmedGroom card = new EdgarCharmedGroom();
        Permanent coffin = harness.addToBattlefieldAndReturn(player, card);
        coffin.setCard(card.getBackFaceCard());
        coffin.setTransformed(true);
        coffin.setCounterCount(CounterType.BLOODLINE, bloodlineCounters);
        return coffin;
    }

    private List<Permanent> vampireTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
