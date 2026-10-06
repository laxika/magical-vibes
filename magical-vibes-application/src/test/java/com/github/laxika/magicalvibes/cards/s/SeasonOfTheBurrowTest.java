package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasonOfTheBurrow.class, GrizzlyBears.class, Plains.class, BarkformHarvester.class, SinisterMonolith.class})
class SeasonOfTheBurrowTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose no modes")
    void canChooseNoModes() {
        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Can choose the Rabbit mode five times")
    void createsFiveRabbitTokens() {
        cast(5);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(5)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(permanent.getCard().getName()).isEqualTo("Rabbit");
                });
    }

    @Test
    @DisplayName("Can exile two nonland permanents and their controllers draw")
    void exilesTwoPermanentsAndTheirControllersDraw() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(10, List.of(firstTarget.getId(), secondTarget.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Can choose the same permanent for repeated exile modes")
    void allowsSamePermanentForRepeatedExileModes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Plains()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(10, List.of(target.getId(), target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Returns a qualifying permanent with indestructible")
    void returnsPermanentWithIndestructible() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));

        cast(12, returnedCard.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Exile mode cannot target a land")
    void exileModeCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> cast(6, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target enumeration follows the selected mode's target filters")
    void targetEnumerationFollowsSelectedMode() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        Card graveyardBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBear));
        harness.setHand(player1, List.of(new SeasonOfTheBurrow()));

        var oneExile = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, gd.playerHands.get(player1.getId()).getFirst(), player1.getId(), List.of(), 6);
        assertThat(oneExile.validPermanentIds()).containsExactly(bear.getId());
        assertThat(oneExile.minTargets()).isEqualTo(1);
        assertThat(oneExile.maxTargets()).isEqualTo(1);

        var twoExiles = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, gd.playerHands.get(player1.getId()).getFirst(), player1.getId(), List.of(), 10);
        assertThat(twoExiles.validPermanentIds()).containsExactly(bear.getId());
        assertThat(twoExiles.validPermanentIds()).doesNotContain(land.getId());
        assertThat(twoExiles.minTargets()).isEqualTo(2);
        assertThat(twoExiles.maxTargets()).isEqualTo(2);

        var returnPermanent = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, gd.playerHands.get(player1.getId()).getFirst(), player1.getId(), List.of(), 12);
        assertThat(returnPermanent.validGraveyardCardIds()).containsExactly(graveyardBear.getId());
        assertThat(returnPermanent.minTargets()).isEqualTo(1);
        assertThat(returnPermanent.maxTargets()).isEqualTo(1);
    }

    @Test
    void returnsPermanentWithAnIndestructibleCounter() {
        Card card = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(card));

        cast(12, card.getId());

        Permanent returned = findPermanent(player1, "Barkform Harvester");
        assertThat(returned.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void createsThreeRabbitsAndExilesOnePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setLibrary(player2, List.of(new Plains()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(9, target.getId());

        assertThat(countPermanents(player1, "Rabbit")).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    void createsTwoRabbitsAndReturnsAPermanent() {
        Card card = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(card));

        cast(14, card.getId());

        assertThat(countPermanents(player1, "Rabbit")).isEqualTo(2);
        assertThat(findPermanent(player1, "Barkform Harvester")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void exilesAndReturnsDifferentTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        Card card = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(card));
        harness.setLibrary(player2, List.of(new Plains()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(15, List.of(target.getId(), card.getId()));

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        assertThat(findPermanent(player1, "Barkform Harvester")).isNotNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    void canReturnALand() {
        Card card = new Plains();
        harness.setGraveyard(player1, List.of(card));

        cast(12, card.getId());

        assertThat(findPermanent(player1, "Plains")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void cannotReturnAPermanentWithManaValueFour() {
        Card card = new SinisterMonolith();
        harness.setGraveyard(player1, List.of(card));

        assertThatThrownBy(() -> cast(12, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnANonpermanentCard() {
        Card card = new SeasonOfTheBurrow();
        harness.setGraveyard(player1, List.of(card));

        assertThatThrownBy(() -> cast(12, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnAnOpponentsPermanentCard() {
        Card card = new BarkformHarvester();
        harness.setGraveyard(player2, List.of(card));

        assertThatThrownBy(() -> cast(12, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsNoRabbitsWhenItsOnlyTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setHand(player1, List.of(new SeasonOfTheBurrow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 9, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rabbit")).isZero();
    }

    @Test
    void drawsForEachTargetsController() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player2, List.of());

        cast(10, List.of(own.getId(), opposing.getId()));

        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void cast(int modeIndex) {
        cast(modeIndex, List.of());
    }

    private void cast(int modeIndex, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SeasonOfTheBurrow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        cast(modeIndex, List.of(targetId));
    }
}
