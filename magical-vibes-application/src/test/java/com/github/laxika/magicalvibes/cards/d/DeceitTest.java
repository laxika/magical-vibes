package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeceitTest extends BaseCardTest {

    @Test
    void blueBlueBouncesAnotherNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDeceit(ManaColor.BLUE, 2, ManaColor.COLORLESS, 4, List.of(player2.getId(), targetId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Deceit");
    }

    @Test
    void blackBlackMakesOpponentDiscardChosenNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        castDeceit(ManaColor.BLACK, 2, ManaColor.COLORLESS, 4, List.of(player2.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void hybridCastWithOneOfEachColorTriggersNeitherAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, List.of(player2.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Deceit");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetALandForBlueAbility() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player2.getId(), targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another nonland permanent");
    }

    @Test
    void evokeWithBlueBlueBouncesAndSacrificesDeceit() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deceit");
    }

    private void castDeceit(ManaColor firstColor, int firstAmount, ManaColor secondColor,
                            int secondAmount, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, firstColor, firstAmount);
        harness.addMana(player1, secondColor, secondAmount);
        harness.castCreature(player1, 0, targetIds);
    }
}
