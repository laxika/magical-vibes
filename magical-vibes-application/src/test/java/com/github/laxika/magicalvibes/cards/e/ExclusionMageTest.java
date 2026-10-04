package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExclusionMage.class, ColossalDreadmaw.class})
class ExclusionMageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Exclusion Mage enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        castMage(harness.getPermanentId(player2, "Colossal Dreadmaw"));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Exclusion Mage");
    }

    @Test
    @DisplayName("ETB returns the targeted opponent creature to its owner's hand")
    void etbBouncesOpponentCreature() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        castMage(harness.getPermanentId(player2, "Colossal Dreadmaw"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInHand(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Exclusion Mage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        UUID ownBearsId = harness.getPermanentId(player1, "Colossal Dreadmaw");
        harness.setHand(player1, List.of(new ExclusionMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithNoOpponentCreatures() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        castMage(null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Exclusion Mage");
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnsStolenCreatureToOwnerRatherThanController() {
        var creature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        castMage(creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInHand(player1, "Colossal Dreadmaw");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void targetBecomingControlledByTriggerControllerIsNotReturned() {
        var creature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        castMage(creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterMageLeavesBattlefield() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        castMage(harness.getPermanentId(player2, "Colossal Dreadmaw"));
        harness.passBothPriorities();
        var mage = findPermanent(player1, "Exclusion Mage");
        gd.playerBattlefields.get(player1.getId()).remove(mage);
        gd.playerGraveyards.get(player1.getId()).add(mage.getCard());
        resolveAllTriggers();

        harness.assertInHand(player2, "Colossal Dreadmaw");
        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(gd.stack).isEmpty();
    }

    private void castMage(UUID targetId) {
        harness.setHand(player1, List.of(new ExclusionMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetId);
    }
}
