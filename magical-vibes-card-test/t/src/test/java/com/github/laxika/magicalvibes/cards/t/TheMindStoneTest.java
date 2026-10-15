package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMindStone.class, GrizzlyBears.class, Forest.class, Boomerang.class, Naturalize.class})
class TheMindStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one white mana")
    void tapsForWhiteMana() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not flicker a permanent before it is harnessed")
    void doesNotFlickerBeforeHarnessed() {
        harness.addToBattlefield(player1, new TheMindStone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        advanceToEndStep();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Harnessing flickers another nonland permanent at the end step")
    void harnessingFlickersAnotherPermanent() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(stone.isHarnessed()).isTrue();

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself or a land")
    void rejectsInvalidTargets() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "The Mind Stone")).isEqualTo(stone.getId());
        assertThat(harness.getPermanentId(player1, "Forest")).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Harnessing pays the full mana and tap cost and uses the stack")
    void harnessingPaysCostsBeforeResolution() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(stone.isTapped()).isTrue();
        assertThat(stone.isHarnessed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(stone.isHarnessed()).isTrue();
    }

    @Test
    @DisplayName("May choose no target even when another nonland permanent is available")
    void mayChooseNoTarget() {
        harnessStone();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bears.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at the opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harnessStone();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot flicker an opponent's permanent")
    void doesNotTargetOpponentsPermanent() {
        harnessStone();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Returns a stolen permanent under its owner's control")
    void returnsStolenPermanentToOwner() {
        harnessStone();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        advanceToEndStep();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(bears.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("End-step ability still resolves after the Stone is returned to hand")
    void resolvesAfterStoneLeavesBattlefield() {
        Permanent stone = harnessStone();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Boomerang()));

        advanceToEndStep();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, stone.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "The Mind Stone");
        harness.assertNotOnBattlefield(player1, "The Mind Stone");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bears.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Indestructible prevents destruction by Naturalize")
    void survivesDestruction() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, stone.getId());

        assertThat(harness.getPermanentId(player1, "The Mind Stone")).isEqualTo(stone.getId());
        harness.assertNotInGraveyard(player1, "The Mind Stone");
    }

    @Test
    @DisplayName("A target returned to hand in response is not returned to the battlefield")
    void doesNotReturnTargetThatLeftBattlefield() {
        harnessStone();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Boomerang()));

        advanceToEndStep();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Stone returned to hand and recast must be harnessed again")
    void recastStoneIsNotHarnessed() {
        Permanent stone = harnessStone();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, stone.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bears.getId());
        assertThat(harness.getPermanentId(player1, "The Mind Stone")).isNotEqualTo(stone.getId());
    }

    private Permanent harnessStone() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        return stone;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
