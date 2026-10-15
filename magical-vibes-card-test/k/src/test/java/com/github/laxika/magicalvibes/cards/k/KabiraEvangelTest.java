package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KabiraEvangel.class, GrizzlyBears.class, StoneworkPuma.class, Conspiracy.class})
class KabiraEvangelTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may grant protection to all Allies you control")
    void ownAllyEntryGrantsProtectionToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new KabiraEvangel());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new KabiraEvangel(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "RED");

        Permanent enteringAlly = findPermanents(player1, "Kabira Evangel").stream()
                .filter(permanent -> !permanent.getId().equals(existingAlly.getId()))
                .findFirst().orElseThrow();
        assertThat(existingAlly.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(enteringAlly.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(nonAlly.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Declining the triggered ability grants no protection")
    void mayBeDeclined() {
        harness.castFromHand(player1, new KabiraEvangel(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent evangel = findPermanent(player1, "Kabira Evangel");
        assertThat(evangel.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KabiraEvangel());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anotherAllyEntryProtectsOnlyControlledAlliesAndExpires() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new KabiraEvangel());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());

        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");

        Permanent enteringAlly = findPermanent(player1, "Stonework Puma");
        assertThat(evangel.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.BLUE);
        assertThat(enteringAlly.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.BLUE);
        assertThat(opposingAlly.getProtectionFromColorsUntilEndOfTurn()).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(evangel.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(enteringAlly.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    void opponentsAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KabiraEvangel());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoEvangelTriggersCanChooseDifferentColors() {
        harness.addToBattlefield(player1, new KabiraEvangel());
        harness.castFromHand(player1, new KabiraEvangel(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "WHITE");

        assertThat(findPermanents(player1, "Kabira Evangel")).hasSize(2).allSatisfy(permanent ->
                assertThat(permanent.getProtectionFromColorsUntilEndOfTurn())
                        .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE));
    }

    @Test
    void ownEntryStillTriggersWhenConspiracyReplacesAllyType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.HUMAN.name());

        harness.castFromHand(player1, new KabiraEvangel(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "GREEN");
        assertThat(findPermanent(player1, "Kabira Evangel").getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }
}
