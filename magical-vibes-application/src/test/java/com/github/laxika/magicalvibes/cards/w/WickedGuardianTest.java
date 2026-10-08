package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WickedGuardian.class, Forest.class, GarenbrigSquire.class, WhiteKnight.class, BakeIntoAPie.class})
class WickedGuardianTest extends BaseCardTest {

    @Test
    void acceptingAbilityDamagesAnotherControlledCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.addToBattlefield(player2, new GarenbrigSquire());
        harness.setLibrary(player1, List.of(new Forest()));
        castWickedGuardian();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Wicked Guardian");
        harness.assertInGraveyard(player1, "Garenbrig Squire");
    }

    @Test
    void decliningAbilityDoesNotDamageOrDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        castWickedGuardian();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void protectionFromBlackDoesNotPreventChoosingCreatureOrDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        castWickedGuardian();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "White Knight");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void abilityTriggersWithoutAnotherCreatureAndCanChooseOneAvailableAtResolution() {
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        castWickedGuardian();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertInGraveyard(player1, "Garenbrig Squire");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void sourceLeavingBeforeResolutionStillDealsDamageAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        castWickedGuardian();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.setHand(player2, List.of(new BakeIntoAPie()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Wicked Guardian"));
        harness.assertInGraveyard(player1, "Wicked Guardian");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertInGraveyard(player1, "Garenbrig Squire");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    private void castWickedGuardian() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WickedGuardian(), "{3}{B}");
    }
}
