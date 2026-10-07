package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AgadeemOccultist;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandUnited.class, AgadeemOccultist.class, GrizzlyBears.class, FountainOfYouth.class})
class StandUnitedTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an Ally and lets the spell controller scry 2")
    void boostsAllyAndScries() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());

        castStandUnited(ally);

        assertThat(ally.getPowerModifier()).isEqualTo(2);
        assertThat(ally.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Boosts a non-Ally creature without scrying")
    void boostsNonAllyWithoutScrying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStandUnited(creature);

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStandUnited(creature);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new StandUnited()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting an opponent's Ally lets the caster scry their own library")
    void targetingOpponentsAllyScriesCastersLibrary() {
        Permanent ally = harness.addToBattlefieldAndReturn(player2, new AgadeemOccultist());
        GrizzlyBears top = new GrizzlyBears();
        FountainOfYouth second = new FountainOfYouth();
        harness.setLibrary(player1, List.of(top, second));

        castStandUnited(ally);

        assertThat(ally.getPowerModifier()).isEqualTo(2);
        assertThat(ally.getToughnessModifier()).isEqualTo(2);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.libraryOwnerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
    }

    @Test
    @DisplayName("An Ally with a one-card library scries the available card")
    void scriesAvailableCardInShortLibrary() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        castStandUnited(ally);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("If the only target leaves before resolution, no scry occurs")
    void removedTargetPreventsScry() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());
        GrizzlyBears top = new GrizzlyBears();
        FountainOfYouth second = new FountainOfYouth();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new StandUnited()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerGraveyards.get(player1.getId()).add(ally.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        harness.assertInGraveyard(player1, "Stand United");
        assertThat(gd.stack).isEmpty();
    }

    private void castStandUnited(Permanent target) {
        harness.setHand(player1, List.of(new StandUnited()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
