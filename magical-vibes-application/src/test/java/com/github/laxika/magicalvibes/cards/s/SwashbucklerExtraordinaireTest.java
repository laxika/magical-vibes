package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwashbucklerExtraordinaire.class, Treasure.class, GrizzlyBears.class})
class SwashbucklerExtraordinaireTest extends BaseCardTest {

    @Test
    void entersWithATreasure() {
        harness.enterBattlefieldAndReturn(player1, new SwashbucklerExtraordinaire());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void sacrificesTreasuresAndGrantsDoubleStrikeToThatManyCreatures() {
        addReadySwashbuckler();
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());
        Permanent secondTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstTreasure.getId(), secondTreasure.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        assertThat(firstTarget.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(secondTarget.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void decliningOrSacrificingNoTreasuresDoesNothing() {
        addReadySwashbuckler();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(treasure);
    }

    private Permanent addReadySwashbuckler() {
        return addCreatureReady(player1, new SwashbucklerExtraordinaire());
    }

    @Test
    void triggersWhenAnotherCreatureAttacksWhileSwashbucklerStaysBack() {
        harness.addToBattlefield(player1, new SwashbucklerExtraordinaire());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Treasure());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void choosingNoTreasuresDoesNotCreateAReflexiveAbility() {
        Permanent swashbuckler = addReadySwashbuckler();
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(treasure);
        assertThat(swashbuckler.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void canTargetAnOpponentsCreature() {
        addReadySwashbuckler();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwashbucklerExtraordinaire());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(treasure.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void canChooseMoreThanOneHundredTargetsWhenThatManyTreasuresAreSacrificed() {
        addReadySwashbuckler();
        List<UUID> treasureIds = new ArrayList<>();
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            treasureIds.add(harness.addToBattlefieldAndReturn(player1, new Treasure()).getId());
            targets.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, treasureIds);
        for (Permanent target : targets) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, target.getId());
        }
        resolveAllTriggers();

        assertThat(targets).allMatch(target -> target.hasKeyword(Keyword.DOUBLE_STRIKE));
    }

    @Test
    void canChooseFewerTargetsThanTreasuresSacrificedAndGrantExpiresAtEndOfTurn() {
        Permanent swashbuckler = addReadySwashbuckler();
        Permanent firstTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());
        Permanent secondTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstTreasure.getId(), secondTreasure.getId()));
        harness.handlePermanentChosen(player1, swashbuckler.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(swashbuckler.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(swashbuckler.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
