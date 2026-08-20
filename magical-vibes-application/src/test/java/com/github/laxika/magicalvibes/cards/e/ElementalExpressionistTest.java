package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElementalExpressionistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant grants the target exile replacement and creates an Elemental on bounce")
    void castingInstantGrantsExileReplacementAndCreatesToken() {
        Permanent target = setUpAndResolveMagecraft();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
    }

    @Test
    @DisplayName("Casting and copying an instant grants two independent magecraft instances")
    void copyingInstantCreatesTwoTokensWhenTargetLeaves() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
    }

    @Test
    @DisplayName("The granted replacement and trigger expire at end of turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent target = setUpAndResolveMagecraft();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Magecraft cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setUpAndResolveMagecraft() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return target;
    }
}
