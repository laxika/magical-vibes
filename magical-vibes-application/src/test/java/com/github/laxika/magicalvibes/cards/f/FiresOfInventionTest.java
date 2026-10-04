package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.cards.s.StonecoilSerpent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiresOfInvention.class, Forest.class, GrizzlyBears.class, Shock.class,
        Fling.class, StonecoilSerpent.class, BonecrusherGiant.class, Stomp.class})
class FiresOfInventionTest extends BaseCardTest {

    @Test
    void countsSpellsCastBeforeEnteringIncludingItself() {
        harness.setHand(player1, List.of(new Shock(), new FiresOfInvention(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player1, new Forest());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fires of Invention");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayForSpellAboveLandCount() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentCanRespondDuringControllersTurnWithoutReceivingFreeCost() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void landPlayDoesNotCountAsSpellAndUpdatesFreeCostThreshold() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Shock()));

        harness.playLand(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void freeFlingStillRequiresSacrificingCreature() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        var creature = harness.addToBattlefieldAndReturn(player1, new BonecrusherGiant());
        harness.setHand(player1, List.of(new Fling()));

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bonecrusher Giant");
        harness.assertLife(player2, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void freeFlingCannotBeCastWithoutCreature() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Fling()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void positiveXRequiresPayingManaEvenWithEnoughLands() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new StonecoilSerpent()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Stonecoil Serpent")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void adventureAndCreatureFromExileEachCountAsSpellAndCanBothBeFree() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        var giant = new BonecrusherGiant();
        harness.setHand(player1, List.of(giant, new Shock()));

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(giant.getId())).isNotNull();
        harness.castFromExile(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonecrusher Giant");
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void freeCostDoesNotAllowCreatureCastWhileStackIsNotEmpty() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void spellLimitResetsOnNextTurn() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Controller can cast a spell for free when its mana value is within the land count")
    void castsSpellForFreeWithinLandCount() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spells above the land-count mana-value cap still require mana")
    void rejectsSpellAboveLandCountWithoutMana() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller can cast no more than two spells each turn")
    void limitsControllerToTwoSpells() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller cannot cast spells during an opponent's turn")
    void controllerCannotCastDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can cast spells during their own turn")
    void opponentCanCastDuringOwnTurn() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
