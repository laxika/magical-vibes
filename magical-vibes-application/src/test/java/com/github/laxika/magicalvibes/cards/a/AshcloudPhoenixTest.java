package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshcloudPhoenix.class})
class AshcloudPhoenixTest extends BaseCardTest {

    @Test
    void turningFaceUpDealsTwoDamageToEachPlayer() {
        harness.setHand(player1, List.of(new AshcloudPhoenix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent phoenix = findPermanent(player1, "Ashcloud Phoenix");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(phoenix));
        harness.passBothPriorities();

        assertThat(phoenix.isFaceDown()).isFalse();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void deathReturnsItToTheBattlefieldFaceDown() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new AshcloudPhoenix());
        phoenix.setMarkedDamage(phoenix.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ashcloud Phoenix");
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(returned.getCard().getId()).isEqualTo(phoenix.getCard().getId());
        harness.assertNotInGraveyard(player1, "Ashcloud Phoenix");
    }

    @Test
    void faceDownPhoenixDoesNotHaveItsDeathAbility() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new AshcloudPhoenix());
        phoenix.setFaceDown(2, 2, java.util.Set.of(com.github.laxika.magicalvibes.model.CardType.CREATURE));
        phoenix.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ashcloud Phoenix");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnedPhoenixCanTurnFaceUpAndReturnAgainAfterAnotherDeath() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new AshcloudPhoenix());
        phoenix.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ashcloud Phoenix");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));

        assertThat(returned.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);

        returned.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returnedAgain = findPermanent(player1, "Ashcloud Phoenix");
        assertThat(returnedAgain.isFaceDown()).isTrue();
        assertThat(returnedAgain.getId()).isNotEqualTo(returned.getId());
        harness.assertNotInGraveyard(player1, "Ashcloud Phoenix");
    }

    @Test
    void deathAbilityDoesNotReturnCardRemovedFromGraveyardBeforeResolution() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new AshcloudPhoenix());
        phoenix.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Ashcloud Phoenix");

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(phoenix.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Ashcloud Phoenix");
    }

    @Test
    void stolenPhoenixReturnsUnderItsControllerRatherThanItsOwner() {
        AshcloudPhoenix card = new AshcloudPhoenix();
        card.setOwnerId(player2.getId());
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, card);
        phoenix.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Ashcloud Phoenix");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ashcloud Phoenix");
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(returned.getCard().getId()).isEqualTo(card.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Ashcloud Phoenix");
    }
}
