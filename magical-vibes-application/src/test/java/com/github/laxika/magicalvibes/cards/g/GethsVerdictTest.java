package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GethsVerdict.class, GrizzlyBears.class, GiantSpider.class})
class GethsVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Geth's Verdict targeting opponent puts instant on the stack")
    void castingPutsInstantOnStack() {
        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(GethsVerdict.class);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it and loses 1 life")
    void opponentSacrificesCreatureAndLosesLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose")
    void opponentWithMultipleCreaturesChooses() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice and loses 1 life")
    void opponentChoosesCreatureToSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player 2 chooses to sacrifice Grizzly Bears
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Opponent with no creatures still loses 1 life")
    void noCreaturesStillLosesLife() {
        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Can target self — controller sacrifices creature and loses 1 life")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Geth's Verdict goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Geth's Verdict");
    }

    @Test
    @DisplayName("Life loss occurs only after the target chooses and sacrifices a creature")
    void lifeLossWaitsUntilAfterSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player2, 10);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertLife(player2, 10);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertLife(player2, 9);
        harness.assertInGraveyard(player1, "Geth's Verdict");
    }

    @Test
    @DisplayName("A creature controlled by the caster is not sacrificed for a creatureless opponent")
    void onlyTargetPlayersCreaturesCanBeSacrificed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
