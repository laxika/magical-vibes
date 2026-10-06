package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReachForTheSky.class, GrizzlyBears.class, FountainOfYouth.class})
class ReachForTheSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+2 and reach")
    void grantsBoostAndReach() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReachForTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Draws a card when the Aura is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ReachForTheSky());
        aura.setAttachedTo(bears.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Reach for the Sky");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ReachForTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void castsDuringOpponentsCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReachForTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Reach for the Sky").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Aura controller draws when the enchanted opponent's creature leaves")
    void drawsWhenEnchantedCreatureLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReachForTheSky()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Reach for the Sky");
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName()).containsExactly("Fountain of Youth");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName()).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("An Aura whose target leaves before resolution does not draw a card")
    void noDrawWhenTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReachForTheSky()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, bears));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Reach for the Sky");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returning the Aura to hand removes the bonus without drawing")
    void returningAuraToHandDoesNotDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReachForTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Reach for the Sky");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aura));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName()).containsExactly("Reach for the Sky");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }
}
