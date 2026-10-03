package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinFreerunner;
import com.github.laxika.magicalvibes.cards.s.SpatialContortion;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbodimentOfFury.class, Wastes.class, GoblinFreerunner.class, SpatialContortion.class})
class EmbodimentOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may animate a land with haste, Elemental, and trample")
    void landfallAnimatesTargetLand() {
        addEmbodiment();
        Permanent land = addLand(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinFreerunner());
        triggerLandfall();

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, land)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Declining landfall leaves the target land unchanged")
    void decliningLandfallLeavesLandUnchanged() {
        addEmbodiment();
        Permanent land = addLand(player1);
        triggerLandfall();

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Landfall animation expires at end of turn")
    void landfallAnimationExpiresAtEndOfTurn() {
        addEmbodiment();
        Permanent land = addLand(player1);
        triggerLandfall();

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Landfall cannot target an opponent's land")
    void landfallCannotTargetOpponentsLand() {
        addEmbodiment();
        Permanent opponentLand = addLand(player2);
        triggerLandfall();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Landfall can animate the land that just entered")
    void landfallCanTargetEnteringLand() {
        addEmbodiment();
        Permanent land = harness.enterBattlefieldAndReturn(player1, new Wastes());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's entering land does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        addEmbodiment();
        harness.enterBattlefieldAndReturn(player2, new Wastes());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Landfall still animates its target after Embodiment dies, without trample")
    void landfallResolvesAfterSourceDies() {
        Permanent embodiment = addEmbodiment();
        Permanent land = addLand(player1);
        triggerLandfall();
        harness.handlePermanentChosen(player1, land.getId());

        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, embodiment.getId());
        harness.assertInGraveyard(player1, "Embodiment of Fury");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Removing Embodiment removes trample but leaves the land animated")
    void sourceRemovalOnlyRemovesTrample() {
        Permanent embodiment = addEmbodiment();
        Permanent land = addLand(player1);
        triggerLandfall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();

        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, embodiment.getId());
        harness.assertInGraveyard(player1, "Embodiment of Fury");

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addEmbodiment() {
        return harness.addToBattlefieldAndReturn(player1, new EmbodimentOfFury());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Wastes());
    }

    private void triggerLandfall() {
        harness.setHand(player1, List.of(new Wastes()));
        harness.playLand(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
