package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenConfluencer.class, DarksteelCitadel.class, GrizzlyBears.class, ManaConfluence.class, Mountain.class})
class DwarvenConfluencerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroying a nontoken land creates a Mana Confluence token for its controller")
    void destroysNontokenLandAndCreatesManaConfluenceToken() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, battlefieldIndex(player1, confluencer), 0, null, mountain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        Permanent token = findPermanent(player2, "Mana Confluence");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.LAND);

        harness.setLife(player2, 20);
        harness.activateAbility(player2, battlefieldIndex(player2, token), 0, null, null);
        harness.handleListChoice(player2, "RED");

        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dwarven Confluencer cannot target a land token")
    void cannotTargetLandToken() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent landToken = harness.addToBattlefieldAndReturn(player2, landToken());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, confluencer), 0, null, landToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dwarven Confluencer cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, confluencer), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An indestructible land survives but its controller still creates the token")
    void createsTokenEvenWhenLandCannotBeDestroyed() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent citadel = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        harness.activateAbility(player1, battlefieldIndex(player1, confluencer), 0, null, citadel.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        assertThat(countPermanents(player2, "Mana Confluence")).isEqualTo(1);
        assertThat(countPermanents(player1, "Mana Confluence")).isZero();
    }

    @Test
    @DisplayName("Targeting your own land gives you the replacement token")
    void canTargetOwnLand() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.activateAbility(player1, battlefieldIndex(player1, confluencer), 0, null, mountain.getId());
        assertThat(confluencer.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(countPermanents(player1, "Mana Confluence")).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(countPermanents(player1, "Mana Confluence")).isEqualTo(1);
        assertThat(countPermanents(player2, "Mana Confluence")).isZero();
        assertThat(findPermanent(player1, "Mana Confluence").isTapped()).isFalse();
    }

    @Test
    @DisplayName("An ability whose land target has left the battlefield creates no token")
    void missingTargetCreatesNoToken() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, battlefieldIndex(player1, confluencer), 0, null, mountain.getId());
        gd.playerBattlefields.get(player2.getId()).remove(mountain);
        gd.playerHands.get(player2.getId()).add(mountain.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Mountain");
        assertThat(countPermanents(player1, "Mana Confluence")).isZero();
        assertThat(countPermanents(player2, "Mana Confluence")).isZero();
    }

    @Test
    @DisplayName("Removing the source does not stop its activated ability")
    void abilityResolvesWithoutSource() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, battlefieldIndex(player1, confluencer), 0, null, mountain.getId());
        gd.playerBattlefields.get(player1.getId()).remove(confluencer);
        gd.playerGraveyards.get(player1.getId()).add(confluencer.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        assertThat(countPermanents(player2, "Mana Confluence")).isEqualTo(1);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private static Card landToken() {
        Card card = new Card();
        card.setName("Land Token");
        card.setType(CardType.LAND);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(null);
        card.setColors(java.util.List.of());
        card.setPower(0);
        card.setToughness(0);
        return card;
    }
}
