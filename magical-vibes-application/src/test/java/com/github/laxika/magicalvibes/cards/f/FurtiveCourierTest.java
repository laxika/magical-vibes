package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sacrifice;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurtiveCourier.class, Atog.class, GrizzlyBears.class, Sacrifice.class, Spellbook.class,
        Forest.class})
class FurtiveCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked after you sacrifice an artifact")
    void becomesUnblockableAfterArtifactSacrifice() {
        harness.addToBattlefield(player1, new Atog());
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new FurtiveCourier());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.hasCantBeBlocked(gd, courier)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, courier)).isTrue();
    }

    @Test
    @DisplayName("A nonartifact sacrifice does not enable unblockability")
    void nonartifactSacrificeDoesNotEnableUnblockability() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new FurtiveCourier());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sacrifice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, courier)).isFalse();
    }

    @Test
    @DisplayName("Attacking draws a card, then discards a card")
    void attacksLoot() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new FurtiveCourier());
        courier.setSummoningSick(false);
        GrizzlyBears discard = new GrizzlyBears();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int courierIndex = gd.playerBattlefields.get(player1.getId()).indexOf(courier);
        gs.declareAttackers(gd, player1, List.of(courierIndex), null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .contains("Forest");
    }
}
