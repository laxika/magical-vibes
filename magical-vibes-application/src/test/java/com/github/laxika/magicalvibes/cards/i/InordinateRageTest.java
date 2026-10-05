package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InordinateRage.class, Forest.class, GrizzlyBears.class})
class InordinateRageTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureAndScriesOne() {
        Permanent target = addCreature();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void boostWearsOffAtCleanup() {
        Permanent target = addCreature();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
    }

    @Test
    void scryCanKeepTheTopCardWithoutDrawingIt() {
        Permanent target = addCreature();
        Forest top = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canBoostOpponentsCreatureWhileCasterScriesToBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest top = new Forest();
        Forest second = new Forest();
        Forest opponentTop = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void boostsCreatureWithAnEmptyLibrary() {
        Permanent target = addCreature();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof InordinateRage);
    }

    @Test
    void doesNotScryWhenItsOnlyTargetLeavesBeforeResolution() {
        Permanent target = addCreature();
        Forest top = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new InordinateRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof InordinateRage);
    }
}
