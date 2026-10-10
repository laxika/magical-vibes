package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deceit.class, Forest.class, GrizzlyBears.class, Island.class})
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another nonland permanent");
    }

    @Test
    void evokeWithBlackBlackDiscardsAndSacrificesDeceit() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities();

        var order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.contains("sacrifice")).findFirst().orElseThrow());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deceit");
    }

    @Test
    void spendingBothColorsCreatesTwoSeparateTriggers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDeceit(ManaColor.BLUE, 2, ManaColor.BLACK, 4, List.of(player2.getId(), targetId));

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Deceit");
    }

    @Test
    void mixedColorEvokeNeedsNoTargetAndStillSacrifices() {
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deceit");
        harness.assertNotOnBattlefield(player1, "Deceit");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastTriggersNeitherAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new Deceit());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Deceit");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void blackAbilityLeavesAnAllLandHandIntact() {
        harness.setHand(player2, List.of(new Forest(), new Island()));
        castDeceit(ManaColor.BLACK, 2, ManaColor.COLORLESS, 4, List.of(player2.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void blackAbilityHandlesAnEmptyHand() {
        harness.setHand(player2, List.of());
        castDeceit(ManaColor.BLACK, 2, ManaColor.COLORLESS, 4, List.of(player2.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deceit");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void blueEvokeCanChooseNoBounceTargetAndStillSacrifices() {
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deceit");
        harness.assertNotOnBattlefield(player1, "Deceit");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blueAbilityCanBounceAnotherDeceitYouControl() {
        harness.addToBattlefield(player1, new Deceit());
        UUID targetId = harness.getPermanentId(player1, "Deceit");
        castDeceit(ManaColor.BLUE, 2, ManaColor.COLORLESS, 4, List.of(player2.getId(), targetId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deceit");
        harness.assertOnBattlefield(player1, "Deceit");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private void castDeceit(ManaColor firstColor, int firstAmount, ManaColor secondColor,
                            int secondAmount, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new Deceit()));
        harness.addMana(player1, firstColor, firstAmount);
        harness.addMana(player1, secondColor, secondAmount);
        harness.castCreature(player1, 0, targetIds);
    }
}
