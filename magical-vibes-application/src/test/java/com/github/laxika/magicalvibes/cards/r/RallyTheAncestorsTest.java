package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyTheAncestors.class, RagingGoblin.class, GrizzlyBears.class,
        AirElemental.class, Island.class})
class RallyTheAncestorsTest extends BaseCardTest {

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }

    @Test
    @DisplayName("Returns your creature cards with mana value up to X and exiles the spell")
    void returnsCreaturesUpToXAndExilesSpell() {
        Card lowCostCreature = new RagingGoblin();
        Card secondLowCostCreature = new GrizzlyBears();
        Card expensiveCreature = new AirElemental();
        Card nonCreature = new Island();
        harness.setGraveyard(player1, List.of(lowCostCreature, secondLowCostCreature, expensiveCreature, nonCreature));
        harness.setHand(player1, List.of(new RallyTheAncestors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rally the Ancestors"));
        assertThat(gd.getDelayedActions(ExilePermanentAtNextUpkeep.class)).hasSize(2);
    }

    @Test
    @DisplayName("Exiles returned creatures at the caster's next upkeep")
    void exilesReturnedCreaturesAtNextUpkeep() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RallyTheAncestors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService().handleUpkeepTriggers(gd));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(ExilePermanentAtNextUpkeep.class)).hasSize(1);

        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService().handleUpkeepTriggers(gd));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.getDelayedActions(ExilePermanentAtNextUpkeep.class)).isEmpty();
    }
}
