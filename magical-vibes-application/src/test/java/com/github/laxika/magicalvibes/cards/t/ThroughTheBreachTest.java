package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroughTheBreach.class, GlacialRay.class, TimeStop.class, WanderingOnes.class})
class ThroughTheBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature from hand onto the battlefield with haste and end-step sacrifice")
    void putsCreatureWithHasteAndEndStepSacrifice() {
        harness.setHand(player1, List.of(new ThroughTheBreach(), new WanderingOnes()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Wandering Ones");
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(creature.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Declining the may leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new ThroughTheBreach(), new WanderingOnes()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Wandering Ones");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Splice onto Arcane adds Through the Breach's effect to the host spell")
    void spliceOntoArcaneAddsEffect() {
        GlacialRay arcaneSpell = new GlacialRay();
        ThroughTheBreach breach = new ThroughTheBreach();
        WanderingOnes creatureCard = new WanderingOnes();
        harness.setHand(player1, List.of(arcaneSpell, breach, creatureCard));
        // Glacial Ray {1}{R} + splice {2}{R}{R}
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();
        // Glacial Ray resolves first, then the spliced may
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Hand is [Through the Breach, Wandering Ones] — only the creature is a legal choice
        harness.handleCardChosen(player1, 1);

        Permanent creature = findPermanent(player1, "Wandering Ones");
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        // Through the Breach remains in hand after splice
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Through the Breach");
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Splice is rejected when the host spell is not Arcane")
    void spliceRejectedOnNonArcane() {
        TimeStop timeStop = new TimeStop();
        ThroughTheBreach breach = new ThroughTheBreach();
        harness.setHand(player1, List.of(timeStop, breach));
        // Time Stop {4}{U}{U} + splice {2}{R}{R}
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(timeStop, breach);
    }
}
