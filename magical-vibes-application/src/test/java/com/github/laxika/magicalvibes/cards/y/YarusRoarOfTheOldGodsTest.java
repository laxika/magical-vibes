package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YarusRoarOfTheOldGods.class, GrizzlyBears.class, Divination.class, Island.class})
class YarusRoarOfTheOldGodsTest extends BaseCardTest {

    @Test
    void otherCreaturesYouControlHaveHaste() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void multipleFaceDownCreaturesDealDamageAndDrawOnlyOnce() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        addCreatureReady(player1, new YarusRoarOfTheOldGods());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        secondAttacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackers(List.of(1, 2));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void faceDownCreatureDiesAndReturnsToItsOwnersBattlefieldFaceUp() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Card bearCard = new GrizzlyBears();
        bearCard.setOwnerId(player2.getId());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, bearCard);
        bear.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        bear.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent ->
                permanent.getCard().getId().equals(bearCard.getId()));
        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bearCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isFaceDown()).isFalse();
    }

    @Test
    void faceDownNonPermanentCardDoesNotReturn() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent divination = harness.addToBattlefieldAndReturn(player1, new Divination());
        divination.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        divination.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Divination");
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void faceDownNonCreaturePermanentReturnsAndStaysFaceDown() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        island.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(island.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isFaceDown()).isTrue();
    }
}
