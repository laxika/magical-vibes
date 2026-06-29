package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GethsVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Geth's Verdict targeting opponent puts instant on the stack")
    void castingPutsInstantOnStack() {
        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Geth's Verdict");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it and loses 1 life")
    void opponentSacrificesCreatureAndLosesLife() {
        Permanent creature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose")
    void opponentWithMultipleCreaturesChooses() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice and loses 1 life")
    void opponentChoosesCreatureToSacrifice() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player 2 chooses to sacrifice Grizzly Bears
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Geth's Verdict goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Geth's Verdict"));
    }
}
