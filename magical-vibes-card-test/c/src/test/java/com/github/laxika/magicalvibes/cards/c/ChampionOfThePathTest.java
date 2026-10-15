package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChampionOfThePath.class, AirElemental.class, GrizzlyBears.class})
class ChampionOfThePathTest extends BaseCardTest {

    @Test
    @DisplayName("Beholds an Elemental and returns it to its owner's hand when Champion leaves")
    void beholdsElementalAndReturnsItToHand() {
        Card beheldCard = new AirElemental();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionOfThePath()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Champion of the Path");
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Another Elemental deals damage equal to its power to each opponent")
    void anotherElementalDealsItsPowerToEachOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteringElemental = findPermanent(player1, "Air Elemental");
        int enteringPower = gqs.getEffectivePower(gd, enteringElemental);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20 - enteringPower);
    }

    @Test
    @DisplayName("A non-Elemental creature does not trigger the damage ability")
    void nonElementalDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeholdAnElementalCardFromHand() {
        Card beheldCard = new ChampionOfThePath();
        harness.setHand(player1, List.of(new ChampionOfThePath(), beheldCard));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Champion of the Path");
        harness.assertLife(player2, 20);

        Permanent champion = findPermanent(player1, "Champion of the Path");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    void cannotCastWithoutBeholdingAnElemental() {
        harness.setHand(player1, List.of(new ChampionOfThePath()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Champion of the Path");
    }

    @Test
    void cannotBeholdANonElemental() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChampionOfThePath()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdPermanent(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotBeholdAnOpponentsElemental() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ChampionOfThePath()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdPermanent(player1, 0, elemental.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void opponentsElementalDoesNotTrigger() {
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringElementalIsTheDamageSource() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent elemental = findPermanent(player1, "Air Elemental");
        harness.passBothPriorities();

        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(elemental.getId(), 0)).isEqualTo(4);
        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(champion.getId(), 0)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void usesEnteringElementalsPowerAtResolution() {
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent elemental = findPermanent(player1, "Air Elemental");
        harness.inMutationScope(() -> elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2));
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void usesLastKnownPowerWhenEnteringElementalLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent elemental = findPermanent(player1, "Air Elemental");
        harness.inMutationScope(() -> {
            elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, elemental);
        });
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }
}
