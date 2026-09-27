package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgEmissary.class, Forest.class})
class UrborgEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB does not return a permanent")
    void withoutKickerDoesNotReturnPermanent() {
        harness.addToBattlefield(player2, new UrborgEmissary());

        harness.castFromHand(player1, new UrborgEmissary(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Urborg Emissary");
        harness.assertOnBattlefield(player1, "Urborg Emissary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB returns the target permanent to its owner's hand")
    void kickedReturnsTargetPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player1, "Urborg Emissary");
    }

    @Test
    @DisplayName("When kicked, a creature is also a legal target")
    void kickedReturnsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UrborgEmissary());
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urborg Emissary");
        harness.assertInHand(player2, "Urborg Emissary");
    }

    @Test
    @DisplayName("When kicked, the ETB chooses its target as it is put on the stack")
    void kickedChoosesTargetAtEtbTime() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactlyInAnyOrder(
                land.getId(), harness.getPermanentId(player1, "Urborg Emissary"));

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("When kicked, the ETB can return Urborg Emissary itself")
    void kickedCanReturnItself() {
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Urborg Emissary");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(sourceId);

        harness.handlePermanentChosen(player1, sourceId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urborg Emissary");
        harness.assertInHand(player1, "Urborg Emissary");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
