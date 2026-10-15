package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralMaaka;
import com.github.laxika.magicalvibes.cards.g.GruulLocket;
import com.github.laxika.magicalvibes.model.Keyword;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormStrike.class, FeralMaaka.class, GruulLocket.class, Scorchmark.class})
class StormStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Storm Strike boosts the target, grants first strike, and scries 1")
    void boostsGrantsFirstStrikeAndScries() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        harness.setLibrary(player1, List.of(new GruulLocket()));
        harness.setHand(player1, List.of(new StormStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Storm Strike");
    }

    @Test
    @DisplayName("Storm Strike's boost and first strike wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        harness.setLibrary(player1, List.of(new GruulLocket()));
        harness.setHand(player1, List.of(new StormStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Storm Strike cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GruulLocket()).getId();
        harness.setHand(player1, List.of(new StormStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canTargetOpponentsCreatureAndScryCastersLibraryToBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralMaaka());
        GruulLocket top = new GruulLocket();
        FeralMaaka next = new FeralMaaka();
        GruulLocket opponentsTop = new GruulLocket();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentsTop));
        harness.setHand(player1, List.of(new StormStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTop);
        harness.assertInGraveyard(player1, "Storm Strike");
    }

    @Test
    void resolvesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StormStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Storm Strike");
    }

    @Test
    void doesNotScryWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        GruulLocket top = new GruulLocket();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new StormStrike()));
        harness.setHand(player2, List.of(new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Storm Strike");
    }
}
