package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoxingRing.class, GiantTortoise.class, GrizzlyBears.class, HillGiant.class})
class BoxingRingTest extends BaseCardTest {

    @Test
    @DisplayName("An entering creature may fight only an opposing creature with the same mana value")
    void fightsSameManaValueCreature() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent sameManaValue = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, sameManaValue.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.findPermanentById(gd, differentManaValue.getId())).isNotNull();
    }

    @Test
    @DisplayName("Treasure ability requires a creature that fought this turn")
    void createsTreasureAfterCreatureFights() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, tortoise.getId());
        harness.passBothPriorities();

        Permanent grizzly = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.permanentsThatFoughtThisTurn).contains(grizzly.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger rejects an opposing creature with a different mana value")
    void rejectsDifferentManaValueTarget() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, differentManaValue.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller may choose no fight target even when a legal target exists")
    void canDeclineFight() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(gd.permanentsThatFoughtThisTurn).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An entry with no matching opposing creature resolves without a fight")
    void noMatchingCreatureDoesNotFight() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castCreature(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(gd.permanentsThatFoughtThisTurn).isEmpty();
    }

    @Test
    @DisplayName("A creature controlled by the Ring's controller cannot be selected")
    void rejectsControlledCreature() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ally.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dead fighters do not satisfy the Treasure activation restriction")
    void cannotActivateAfterOnlyControlledFighterDies() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ring.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The opposing fighter also qualifies for its controller's Ring")
    void opposingFighterEnablesTreasure() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();
        Permanent opposingRing = harness.addToBattlefieldAndReturn(player2, new BoxingRing());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(opposingRing), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(opposingRing.isTapped()).isTrue();
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Player player,
                              com.github.laxika.magicalvibes.model.Card creature) {
        harness.setHand(player, List.of(creature));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
