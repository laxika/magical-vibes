package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BurningVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a flashback spell triggers target selection")
    void flashbackSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Deals 2 damage to target player when spell cast from graveyard")
    void deals2DamageToTargetPlayer() {
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);

        // Choose opponent as target for the triggered ability
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability
        harness.passBothPriorities();

        // Resolve the flashback spell
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature when spell cast from graveyard")
    void deals2DamageToTargetCreature() {
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFlashback(player1, 0, fountainId);

        // Choose the creature as target for the triggered ability
        harness.handlePermanentChosen(player1, bearsId);

        // Resolve triggered ability — 2 damage kills the 2/2
        harness.passBothPriorities();

        // Resolve the flashback spell
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not trigger when casting a spell from hand")
    void doesNotTriggerOnSpellFromHand() {
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // No target selection prompt — only the creature spell on the stack
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Does not trigger when opponent casts spell from graveyard")
    void doesNotTriggerOnOpponentGraveyardCast() {
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castFlashback(player2, 0, fountainId);

        GameData gd = harness.getGameData();
        // No target selection prompt — only the flashback spell on the stack
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Multiple Burning Vengeances each trigger separately")
    void multipleBurningVengeancesTrigger() {
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.addToBattlefield(player1, new BurningVengeance());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, fountainId);

        // First Burning Vengeance trigger — choose opponent
        harness.handlePermanentChosen(player1, player2.getId());
        // Second Burning Vengeance trigger — choose opponent
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve both triggered abilities
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Resolve the flashback spell
        harness.passBothPriorities();

        // 2 + 2 = 4 damage total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }
}
