package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasonOfTheBurrow.class, GrizzlyBears.class, Plains.class})
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
        setDeck(player2, List.of(new Plains(), new Plains()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(10, List.of(firstTarget.getId(), secondTarget.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Can choose the same permanent for repeated exile modes")
    void allowsSamePermanentForRepeatedExileModes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setDeck(player2, List.of(new Plains()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(10, List.of(target.getId(), target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Returns a qualifying permanent with indestructible")
    void returnsPermanentWithIndestructible() {
        Card returnedCard = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(returnedCard);

        cast(12, returnedCard.getId());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedCard.getId()))
                .findFirst()
                .orElseThrow();
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
        gd.playerGraveyards.get(player1.getId()).add(graveyardBear);
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
        harness.setHand(player1, List.of(new SeasonOfTheBurrow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, modeIndex, targetId);
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
