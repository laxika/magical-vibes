package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeteorGolem.class, Manalith.class, Forest.class})
class MeteorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target nonland permanent an opponent controls")
    void etbDestroysTargetNonlandPermanentOpponentControls() {
        harness.addToBattlefield(player2, new Manalith());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player2, "Manalith");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteor Golem");
        harness.assertNotOnBattlefield(player2, "Manalith");
        harness.assertInGraveyard(player2, "Manalith");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by its caster")
    void cannotTargetOwnNonlandPermanent() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player1, "Manalith");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetOpponentLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("ETB leaves no ability on the stack when there is no legal target")
    void etbLeavesNoAbilityOnStackWithoutLegalTarget() {
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteor Golem");
    }

    @Test
    @DisplayName("ETB destroys a creature even after Meteor Golem leaves the battlefield")
    void etbResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player2, new MeteorGolem());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        UUID targetId = harness.getPermanentId(player2, "Meteor Golem");

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        var source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Meteor Golem");
        harness.assertNotOnBattlefield(player2, "Meteor Golem");
        harness.assertInGraveyard(player2, "Meteor Golem");
    }

    @Test
    @DisplayName("ETB does not destroy a target that left and returned to the battlefield")
    void etbDoesNotDestroyReturnedTarget() {
        Manalith targetCard = new Manalith();
        var target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.setHand(player2, List.of());
        var returned = harness.addToBattlefieldAndReturn(player2, targetCard);
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteor Golem");
        harness.assertOnBattlefield(player2, "Manalith");
        harness.assertNotInGraveyard(player2, "Manalith");
    }
}
