package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollisionOfRealms.class, Forest.class, GrizzlyBears.class})
class CollisionOfRealmsTest extends BaseCardTest {

    @Test
    void shufflesOwnedCreaturesAndEachEligiblePlayerRevealsOneCreature() {
        GrizzlyBears player1Creature = new GrizzlyBears();
        GrizzlyBears player2Creature = new GrizzlyBears();
        addCreatureReady(player1, player1Creature);
        addCreatureReady(player2, player2Creature);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getCard)
                .containsExactly(player2Creature);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void tokenOnlyPlayerDoesNotRevealAndTokenLeavesTheGame() {
        Card token = new Card();
        token.setName("Soldier Token");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    void multipleShuffledCreaturesReturnOnlyOneCreature() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of());

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Card returned = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        assertThat(returned).isIn(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).contains(first == returned ? second : first);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void creatureControlledByOpponentReturnsUnderItsOwnersControl() {
        GrizzlyBears stolen = new GrizzlyBears();
        stolen.setOwnerId(player1.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, stolen);
        gd.stolenCreatures.put(permanent.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        GrizzlyBears opponentLibraryCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentLibraryCreature));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(stolen);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCreature);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void playersWithoutCreaturesDoNotPutLibraryCreaturesOntoBattlefield() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest land = new Forest();
        harness.addToBattlefield(player1, land);
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    private void cast() {
        harness.setHand(player1, List.of(new CollisionOfRealms()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
