package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.d.Doomskar;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseOfTheDreadMarn.class, BeskirShieldmate.class, Doomskar.class, FearlessPup.class})
class RiseOfTheDreadMarnTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie Berserker for each nontoken creature that died this turn")
    void createsZombieBerserkersForAllNontokenCreatureDeaths() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.nontokenCreatureDeathCountThisTurn.put(player2.getId(), 2);
        harness.castFromHand(player1, new RiseOfTheDreadMarn(), "{2}{B}");
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Zombie Berserker");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.ZOMBIE, CardSubtype.BERSERKER);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Creates no tokens when no nontoken creature died this turn")
    void createsNoTokensWithoutNontokenCreatureDeaths() {
        harness.castFromHand(player1, new RiseOfTheDreadMarn(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).isEmpty();
    }

    @Test
    @DisplayName("Can be cast from exile for its foretell cost")
    void canBeCastForForetellCost() {
        RiseOfTheDreadMarn card = new RiseOfTheDreadMarn();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).hasSize(1);
    }

    @Test
    @DisplayName("Counts deaths from both battlefields but ignores tokens that die later")
    void countsActualDeathsAndExcludesTokenDeaths() {
        harness.addToBattlefield(player1, new BeskirShieldmate());
        harness.addToBattlefield(player2, new BeskirShieldmate());
        harness.castFromHand(player1, new Doomskar(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Warrior")).hasSize(1);
        assertThat(findPermanents(player2, "Human Warrior")).hasSize(1);

        harness.castFromHand(player1, new Doomskar(), "{3}{W}{W}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Human Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Human Warrior")).isEmpty();

        harness.castFromHand(player1, new RiseOfTheDreadMarn(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).hasSize(2);
        assertThat(findPermanents(player2, "Zombie Berserker")).isEmpty();
    }

    @Test
    @DisplayName("Counts a creature that dies after casting even if it leaves the graveyard")
    void countsDeathsAtResolutionAfterCardsLeaveGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.castFromHand(player1, new RiseOfTheDreadMarn(), "{2}{B}");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, creature));
        harness.assertInGraveyard(player2, "Fearless Pup");
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast a foretold card during the turn it was foretold")
    void cannotCastOnForetellTurn() {
        RiseOfTheDreadMarn card = new RiseOfTheDreadMarn();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot foretell during the opponent's turn")
    void cannotForetellOnOpponentsTurn() {
        harness.setHand(player1, List.of(new RiseOfTheDreadMarn()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rise of the Dread Marn");
    }

    @Test
    @DisplayName("Exiling a creature does not count as a death")
    void excludesCreaturesExiledFromBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, creature));

        harness.castFromHand(player1, new RiseOfTheDreadMarn(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).isEmpty();
    }
}
