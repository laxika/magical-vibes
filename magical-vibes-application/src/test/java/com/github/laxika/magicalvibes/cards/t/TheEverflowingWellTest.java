package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheEverflowingWell.class, TheMyriadPools.class, Forest.class, GrizzlyBears.class,
        Shock.class, Spellbook.class})
class TheEverflowingWellTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling two cards and drawing two cards")
    void entersMillsAndDraws() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TheEverflowingWell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "The Everflowing Well");
    }

    @Test
    @DisplayName("Transforms during upkeep when the controller has eight permanent cards in their graveyard")
    void transformsWithDescendEight() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent well = harness.addToBattlefieldAndReturn(player1, new TheEverflowingWell());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        resolveAllTriggers();

        assertThat(well.getCard()).isInstanceOf(TheMyriadPools.class);
    }

    @Test
    @DisplayName("Copies a permanent spell onto another permanent when its mana pays for that spell")
    void copiesPermanentSpellUsingProducedMana() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new TheMyriadPools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(pools.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCopyUntilEndOfTurn()).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }
}
