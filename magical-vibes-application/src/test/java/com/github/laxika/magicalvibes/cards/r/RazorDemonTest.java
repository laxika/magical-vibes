package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DemonicBargain;
import com.github.laxika.magicalvibes.cards.d.DemonicPact;
import com.github.laxika.magicalvibes.cards.e.EverAfter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorDemon.class, DemonicBargain.class, EverAfter.class, DemonicPact.class,
        GrizzlyBears.class, Shock.class})
class RazorDemonTest extends BaseCardTest {

    @Test
    void opponentDraftsAndMayCastAFreeSpell() {
        harness.setHand(player1, List.of(new RazorDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.SpellbookCardChoice choice =
                (PendingInteraction.SpellbookCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cards()).hasSize(3);

        Card pact = choice.cards().stream()
                .filter(card -> card.getName().equals("Demonic Pact"))
                .findFirst()
                .orElseThrow();
        harness.handleMultipleCardsChosen(player2, List.of(pact.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Demonic Pact");
        harness.assertNotInHand(player2, "Demonic Pact");
    }

    @Test
    void wardCountersTargetingSpellUnlessOpponentDiscards() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RazorDemon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, demon.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(demon.getMarkedDamage()).isEqualTo(2);
    }
}
