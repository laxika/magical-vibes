package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanTheKid.class, Firebolt.class, DoomedTraveler.class, GrizzlyBears.class, Mountain.class})
class KellanTheKidTest extends BaseCardTest {

    @Test
    void castsAnEligiblePermanentFromHandAfterCastingFromGraveyard() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Doomed Traveler")).isNotNull();
    }

    @Test
    void offersALandAfterDecliningThePermanentSpellChoice() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Mountain")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotTriggerForASpellCastFromHand() {
        harness.addToBattlefield(player1, new KellanTheKid());
        harness.setHand(player1, List.of(new Firebolt(), new DoomedTraveler()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
