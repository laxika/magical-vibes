package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VanquishTheFoulTest extends BaseCardTest {

    private void addManaAndCast(UUID targetId) {
        harness.setHand(player1, List.of(new VanquishTheFoul()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Destroys a creature with power 4 or greater and offers scry 1")
    void destroysHighPowerCreatureAndOffersScry() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Completing scry 1 finishes resolving Vanquish the Foul")
    void completingScryFinishesSpell() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> addManaAndCast(giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
