package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThroughTheBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature from hand onto the battlefield with haste and end-step sacrifice")
    void putsCreatureWithHasteAndEndStepSacrifice() {
        harness.setHand(player1, List.of(new ThroughTheBreach(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(bears.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Declining the may leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new ThroughTheBreach(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Splice onto Arcane adds Through the Breach's effect to the host spell")
    void spliceOntoArcaneAddsEffect() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(arcaneShock, new ThroughTheBreach(), new GrizzlyBears()));
        // Shock {R} + splice {2}{R}{R}
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1));
        harness.passBothPriorities();
        // Shock damage resolves first, then spliced may
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Hand is [Through the Breach, Grizzly Bears] — only the bears is a legal choice
        harness.handleCardChosen(player1, 1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        // Through the Breach remains in hand after splice
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Through the Breach");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Splice is rejected when the host spell is not Arcane")
    void spliceRejectedOnNonArcane() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock(), new ThroughTheBreach()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }
}
