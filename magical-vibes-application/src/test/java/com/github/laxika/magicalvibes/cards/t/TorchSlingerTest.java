package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PillarfieldOx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorchSlinger.class, GrizzlyBears.class, FountainOfYouth.class, PillarfieldOx.class})
class TorchSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not deal damage")
    void withoutKickerDoesNotDealDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new TorchSlinger(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Torch Slinger");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB ability deals 2 damage to a target creature")
    void kickedDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new TorchSlinger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TorchSlinger()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Torch Slinger"));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A kicked Torch Slinger can target itself after entering an empty battlefield")
    void kickedCanTargetItself() {
        harness.setHand(player1, List.of(new TorchSlinger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Torch Slinger");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Torch Slinger"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Torch Slinger");
        harness.assertInGraveyard(player1, "Torch Slinger");
    }

    @Test
    @DisplayName("The kicked ability deals exactly 2 damage to a creature its controller owns")
    void kickedDealsExactlyTwoDamageToOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new PillarfieldOx());
        harness.setHand(player1, List.of(new TorchSlinger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pillarfield Ox");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering without being cast does not trigger the kicked ability")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new TorchSlinger());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Torch Slinger");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The kicker cost must be paid in addition to the creature's mana cost")
    void cannotKickWithOnlyBaseMana() {
        harness.setHand(player1, List.of(new TorchSlinger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Torch Slinger");
        assertThat(gd.stack).isEmpty();
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
