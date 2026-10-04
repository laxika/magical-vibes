package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Emptiness.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class, Plains.class})
class EmptinessTest extends BaseCardTest {

    @Test
    @DisplayName("White mana ETB returns a creature card with mana value 3 or less")
    void whiteBranchReturnsSmallCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Emptiness");
    }

    @Test
    @DisplayName("Black mana ETB puts three -1/-1 counters on up to one target creature")
    void blackBranchPutsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Emptiness");
    }

    @Test
    @DisplayName("Evoke uses the hybrid alternate cost and sacrifices Emptiness after its ETB")
    void evokeReturnsCreatureAndSacrificesSelf() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotOnBattlefield(player1, "Emptiness");
        harness.assertInGraveyard(player1, "Emptiness");
    }

    @Test
    @DisplayName("Black branch rejects a noncreature permanent target")
    void blackBranchRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana spent on the generic cost can enable both ETB abilities")
    void bothColorsEnableBothAbilities() {
        Card creature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Emptiness");
    }

    @Test
    @DisplayName("Evoke paid with one white and one black mana enables neither ETB ability")
    void mixedColorEvokeOnlySacrificesSelf() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Emptiness");
        harness.assertInGraveyard(player1, "Emptiness");
    }

    @Test
    @DisplayName("Black evoke puts counters on a creature and sacrifices Emptiness")
    void blackEvokePutsCountersAndSacrificesSelf() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithEvoke(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Emptiness");
        harness.assertInGraveyard(player1, "Emptiness");
    }

    @Test
    @DisplayName("Black ETB allows choosing no creature even when one is available")
    void blackBranchAllowsNoTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Emptiness");
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("White ETB cannot return an expensive creature, a land, or an opponent's creature")
    void whiteBranchHasNoLegalTargetInIneligibleGraveyards() {
        harness.setGraveyard(player1, List.of(new AirElemental(), new Plains()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Emptiness");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast enables neither mana-dependent ability nor evoke sacrifice")
    void enteringWithoutCastingDoesNotTriggerAbilities() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.enterBattlefieldAndReturn(player1, new Emptiness());

        harness.assertOnBattlefield(player1, "Emptiness");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("White ETB does not return a target that leaves the graveyard before resolution")
    void whiteBranchDoesNotReturnMissingTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Emptiness");
    }
}
