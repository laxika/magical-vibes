package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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
        assertThat(PermanentGrants.protectionColorsOf(gd, existingAlly)).contains(CardColor.RED);
        assertThat(PermanentGrants.protectionColorsOf(gd, enteringAlly)).contains(CardColor.RED);
        assertThat(PermanentGrants.protectionColorsOf(gd, nonAlly)).isEmpty();
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
        assertThat(PermanentGrants.protectionColorsOf(gd, evangel)).isEmpty();
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
        assertThat(PermanentGrants.protectionColorsOf(gd, evangel)).containsExactly(CardColor.BLUE);
        assertThat(PermanentGrants.protectionColorsOf(gd, enteringAlly)).containsExactly(CardColor.BLUE);
        assertThat(PermanentGrants.protectionColorsOf(gd, opposingAlly)).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(PermanentGrants.protectionColorsOf(gd, evangel)).isEmpty();
        assertThat(PermanentGrants.protectionColorsOf(gd, enteringAlly)).isEmpty();
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
                assertThat(PermanentGrants.protectionColorsOf(gd, permanent))
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
        assertThat(PermanentGrants.protectionColorsOf(gd, findPermanent(player1, "Kabira Evangel"))).isEmpty();
    }
}
