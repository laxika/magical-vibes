package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

@CardUsed({BloodMoney.class, GrizzlyBears.class, SolRing.class})
class BloodMoneyTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesAndCreatesTappedTreasureForEachDestroyedNontokenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createTokenCreature());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBloodMoney();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Creature Token");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void indestructibleCreatureDoesNotCreateTreasure() {
        Permanent indestructible = addCreatureReady(player1, new GrizzlyBears());
        indestructible.getGrantedKeywords().add(com.github.laxika.magicalvibes.model.Keyword.INDESTRUCTIBLE);

        castBloodMoney();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(indestructible);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void opponentCreaturesCreateAllTreasuresForSpellController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBloodMoney();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void destroysOnlyTokensWithoutCreatingTreasures() {
        harness.addToBattlefield(player1, createTokenCreature());
        harness.addToBattlefield(player2, createTokenCreature());

        castBloodMoney();

        harness.assertNotOnBattlefield(player1, "Creature Token");
        harness.assertNotOnBattlefield(player2, "Creature Token");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void regeneratedCreatureSurvivesWithoutCreatingTreasure() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        castBloodMoney();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void noncreaturePermanentsSurviveWithoutCreatingTreasures() {
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());

        castBloodMoney();

        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertOnBattlefield(player2, "Sol Ring");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private void castBloodMoney() {
        harness.setHand(player1, List.of(new BloodMoney()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Creature Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }

}
