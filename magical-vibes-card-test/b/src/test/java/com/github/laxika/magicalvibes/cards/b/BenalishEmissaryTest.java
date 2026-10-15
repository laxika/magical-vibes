package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishEmissary.class, Forest.class, Repulse.class})
class BenalishEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB does not destroy a land")
    void withoutKickerDoesNotDestroyLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new BenalishEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Benalish Emissary");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB destroys the target land")
    void kickedDestroysTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BenalishEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("When kicked, only a land is a legal target")
    void kickedOnlyTargetsLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishEmissary());
        harness.setHand(player1, List.of(new BenalishEmissary()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("When kicked, chooses the land as the ETB ability is put on the stack")
    void kickedChoosesTargetAtEtbTime() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BenalishEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(land.getId());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("When kicked without a legal land, the creature enters without an ETB ability")
    void kickedWithoutLandDoesNotCreateTrigger() {
        harness.setHand(player1, List.of(new BenalishEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Emissary");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A kicked Emissary can destroy its controller's land")
    void kickedCanDestroyOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BenalishEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Benalish Emissary");
    }

    @Test
    @DisplayName("The kicked ETB still destroys its target after Emissary returns to hand")
    void kickedTriggerResolvesAfterSourceLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BenalishEmissary()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new Forest()));
        addKickedMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Benalish Emissary"));

        harness.assertInHand(player1, "Benalish Emissary");
        harness.assertNotOnBattlefield(player1, "Benalish Emissary");
        harness.assertOnBattlefield(player2, "Forest");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
