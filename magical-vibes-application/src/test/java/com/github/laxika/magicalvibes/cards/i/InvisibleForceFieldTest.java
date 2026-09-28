package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvisibleForceField.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class InvisibleForceFieldTest extends BaseCardTest {

    @Test
    void grantsIndestructibleToUpToFourTargetPermanentsYouControl() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(bear.getId(), forest.getId(), fountain.getId(), secondBear.getId()));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, fountain, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canBeCastWithNoTargets() {
        cast(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getName().equals("Invisible Force Field"));
    }

    @Test
    void cannotTargetMoreThanFourPermanents() {
        List<Permanent> permanents = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                permanents.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPermanentAnOpponentControls() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent you control");
    }

    @Test
    void grantedIndestructibleWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(List.of(bear.getId()));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void successfulResolutionExilesCardForRebound() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        InvisibleForceField card = new InvisibleForceField();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        InvisibleForceField card = new InvisibleForceField();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Invisible Force Field");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new InvisibleForceField()));
        prepareMana();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new InvisibleForceField()));
        prepareMana();
    }

    private void prepareMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
