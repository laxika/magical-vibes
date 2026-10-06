package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverquillCommand.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class SilverquillCommandTest extends BaseCardTest {

    @Test
    void boostsCreatureAndMakesPlayerDrawAndLoseLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card draw = new Forest();
        harness.setLibrary(player2, List.of(draw));
        harness.setHand(player2, List.of());
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInHand(player2, draw.getName());
    }

    @Test
    void returnsSmallCreatureAndOpponentSacrificesTheirChoice() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        prepareSpell();

        castWithModes(new int[]{1, 3}, graveyardCreature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, sacrificed.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void allowsTheSameOpponentForDrawAndSacrificeModes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card draw = new Forest();
        harness.setLibrary(player2, List.of(draw));
        harness.setHand(player2, List.of());
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInHand(player2, draw.getName());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureBoostModeRejectsNonCreatureTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{0, 2}, List.of(forest.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawAndLifeLossCanTargetControllerWhileOpponentHasNoCreatures() {
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, draw.getName());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void sacrificeModeRejectsControllerAsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{0, 3}, List.of(creature.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeRejectsCreatureWithManaValueGreaterThanTwo() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        prepareSpell();

        assertThatThrownBy(() -> castWithModes(new int[]{1, 3}, creature.getId(),
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeRejectsNonCreatureCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        prepareSpell();

        assertThatThrownBy(() -> castWithModes(new int[]{1, 3}, land.getId(),
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeRejectsCreatureInOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareSpell();

        assertThatThrownBy(() -> castWithModes(new int[]{1, 3}, creature.getId(),
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsCreatureAndDrawsForADifferentTarget() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player2, List.of(draw));
        harness.setHand(player2, List.of());
        prepareSpell();

        castWithModes(new int[]{1, 2}, creature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, creature.getName());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        harness.assertInHand(player2, draw.getName());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void remainingPlayerModeResolvesWhenGraveyardTargetIsRemoved() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player2, List.of(draw));
        harness.setHand(player2, List.of());
        prepareSpell();

        castWithModes(new int[]{1, 2}, creature.getId(), List.of(player2.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, creature.getName());
        harness.assertInHand(player2, draw.getName());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void boostCanTargetOpponentsCreatureBeforeItIsSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void boostsExistingCreatureAndReturnsAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        prepareSpell();

        castWithModes(new int[]{0, 1}, returned.getId(), List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        harness.assertOnBattlefield(player1, returned.getName());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returned);
    }
    @Test
    void boostAndFlyingExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }
    private void castWithModes(int[] modes, UUID targetId, List<UUID> targetIds) {
        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(2, modes),
                targetId, null, targetIds, List.of());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SilverquillCommand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
