package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmortusMasterOfEternity.class})
class ImmortusMasterOfEternityTest extends BaseCardTest {

    @Test
    void tapsForBlueManaEqualToCardsDrawnThisTurnAndRestrictsItToNoncreatureSpells() {
        Permanent immortus = addCreatureReady(player1, new ImmortusMasterOfEternity());
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(immortus.isTapped()).isTrue();
    }

    @Test
    void powerUpShufflesEachPlayersHandAndGraveyardDrawsSevenAndAddsCounter() {
        Permanent immortus = harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        harness.setHand(player1, cards(1));
        harness.setGraveyard(player1, cards(1));
        harness.setLibrary(player1, cards(8));
        harness.setHand(player2, cards(1));
        harness.setGraveyard(player2, cards(1));
        harness.setLibrary(player2, cards(8));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(immortus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        Permanent immortus = harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(immortus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void manaAbilityIgnoresOpponentsDrawsAndResolvesWithoutUsingTheStack() {
        Permanent immortus = addCreatureReady(player1, new ImmortusMasterOfEternity());
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        gd.cardsDrawnThisTurn.put(player2.getId(), 7);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(immortus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    void manaAbilityCanTapWhenNoCardsHaveBeenDrawn() {
        Permanent immortus = addCreatureReady(player1, new ImmortusMasterOfEternity());
        gd.cardsDrawnThisTurn.clear();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(immortus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isZero();
    }

    @Test
    void newlyEnteredImmortusCannotActivateTapAbility() {
        Permanent immortus = harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(immortus.isTapped()).isFalse();
    }

    @Test
    void powerUpRequiresFullCostWhenImmortusDidNotEnterThisTurn() {
        Permanent immortus = addCreatureReady(player1, new ImmortusMasterOfEternity());
        harness.setLibrary(player1, cards(8));
        harness.setLibrary(player2, cards(8));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(immortus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    void powerUpDrawsIncreaseManaProductionButThatManaCannotPayForCreatureSpells() {
        addCreatureReady(player1, new ImmortusMasterOfEternity());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, cards(8));
        harness.setLibrary(player2, cards(8));
        gd.cardsDrawnThisTurn.clear();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(7);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCannotPayForPowerUp() {
        Permanent immortus = harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        immortus.setSummoningSick(false);
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(3);
    }

    @Test
    void powerUpCannotBeActivatedAgainWhileFirstActivationIsOnTheStack() {
        harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
    }

    private List<Card> cards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new ImmortusMasterOfEternity())
                .toList();
    }
}
