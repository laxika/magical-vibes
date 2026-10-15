package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinionMissile.class, DarksteelMyr.class, Forest.class, GrizzlyBears.class})
class MinionMissileTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, destroys the target, and deals 2 damage to its controller")
    void sacrificesCreatureDestroysTargetAndDealsDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile()));

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can discard a card instead of sacrificing a creature")
    void discardsCardInsteadOfSacrificing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile(), new Forest()));

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals damage even when the target creature cannot be destroyed")
    void dealsDamageWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile()));

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile()));

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot cast without discarding a card or sacrificing a creature")
    void rejectsMissingAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Minion Missile");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature to pay the additional cost")
    void rejectsNoncreatureSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareHandAndMana(List.of(new MinionMissile()));

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Minion Missile");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the additional cost")
    void rejectsOpponentsCreatureAsSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile()));

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Minion Missile");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target its controller's creature and damage that player")
    void destroysOwnCreatureAndDamagesItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile(), new Forest()));

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrificing the targeted creature pays the cost but leaves no legal target")
    void sacrificingTargetPreventsDamageOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareHandAndMana(List.of(new MinionMissile()));

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), target.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Minion Missile");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void prepareHandAndMana(List<Card> cards) {
        harness.setHand(player1, cards);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
