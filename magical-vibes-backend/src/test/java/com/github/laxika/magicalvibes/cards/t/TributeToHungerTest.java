package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TributeToHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tribute to Hunger targeting opponent puts instant on the stack")
    void castingPutsInstantOnStack() {
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Tribute to Hunger");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it and controller gains life equal to toughness")
    void opponentSacrificesCreatureAndControllerGainsLife() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Grizzly Bears has toughness 2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose")
    void opponentWithMultipleCreaturesChooses() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness.class);
    }

    @Test
    @DisplayName("Opponent chooses Giant Spider — controller gains 4 life (toughness 4)")
    void opponentChoosesHighToughnessCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player 2 chooses to sacrifice Giant Spider (toughness 4)
        harness.handlePermanentChosen(player2, giant.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Opponent chooses Grizzly Bears — controller gains 2 life (toughness 2)")
    void opponentChoosesLowToughnessCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player 2 chooses to sacrifice Grizzly Bears (toughness 2)
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("No sacrifice and no life gain when opponent has no creatures")
    void noCreaturesNoLifeGain() {
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }

    @Test
    @DisplayName("Tribute to Hunger goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tribute to Hunger"));
    }
}
