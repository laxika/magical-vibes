package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OblivionRing;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuffRewritesHistory.class, Forest.class, GrizzlyBears.class, OblivionRing.class})
class GuffRewritesHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles one eligible permanent per player and offers each controller their own exiled card")
    void shufflesTargetsAndOffersEachControllerTheirOwnCard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new OblivionRing());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        castGuff(List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand, ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1)
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.findExiledCard(ownCreature.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentCreature.getCard().getId())).isNotNull();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
    }

    @Test
    @DisplayName("A player may cast the nonland card exiled for them without paying its mana cost")
    void mayCastExiledCardForFree() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        castGuff(List.of(ownCreature.getId(), opponentCreature.getId()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownCreature.getCard().getId()));
        assertThat(gd.findExiledCard(ownCreature.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(opponentCreature.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("The caster must target their own eligible permanent as well as the opponent's")
    void cannotOmitCastersEligiblePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareGuff();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent with an eligible permanent cannot be omitted")
    void cannotOmitOpponentsEligiblePermanent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareGuff();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A player with only lands and enchantments is skipped")
    void skipsPlayerWithoutEligiblePermanent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new OblivionRing());
        harness.setLibrary(player1, List.of());
        GrizzlyBears untouchedCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(untouchedCard));

        castGuff(List.of(ownCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand, opponentEnchantment);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouchedCard);
        assertThat(gd.findExiledCard(ownCreature.getCard().getId())).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A shuffled token still lets its controller exile a nonland and returns exiled lands")
    void tokenTargetStillOffersFreeCastAndReturnsLands() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, token);
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, secondLand, nonland));

        castGuff(List.of(ownToken.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownToken);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.findExiledCard(firstLand.getId())).isNull();
        assertThat(gd.findExiledCard(secondLand.getId())).isNull();
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        assertThat(gd.findExiledCard(token.getId())).isNull();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("If an affected library contains only lands, they all return and no cast is offered")
    void allLandLibraryReturnsWithoutCastOffer() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, token);
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        castGuff(List.of(ownToken.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.findExiledCard(firstLand.getId())).isNull();
        assertThat(gd.findExiledCard(secondLand.getId())).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castGuff(List<java.util.UUID> targetIds) {
        prepareGuff();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void prepareGuff() {
        harness.setHand(player1, List.of(new GuffRewritesHistory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
