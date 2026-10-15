package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArguelsBloodFast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighMarshalArguel.class, ArguelsBloodFast.class})
class HighMarshalArguelTest extends BaseCardTest {

    @Test
    @DisplayName("When High Marshal Arguel dies, it conjures Arguel's Blood Fast and may transform it")
    void deathTriggerConjuresAndMayTransformArguelsBloodFast() {
        Permanent highMarshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        highMarshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Temple of Aclazotz");
        harness.assertInGraveyard(player1, "High Marshal Arguel");
    }

    @Test
    @DisplayName("If the transform is declined, the conjured Arguel's Blood Fast stays on its front face")
    void decliningTransformLeavesArguelsBloodFastUntransformed() {
        Permanent highMarshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        highMarshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Arguel's Blood Fast");
    }

    @Test
    @DisplayName("With both Arguel's Blood Fast and Temple of Aclazotz, death creates two flying Vampire Demons instead")
    void matchingPermanentsCreateVampireDemonsInstead() {
        harness.addToBattlefield(player1, new ArguelsBloodFast());
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new ArguelsBloodFast());
        temple.setCard(temple.getCard().getBackFaceCard());
        temple.setTransformed(true);

        Permanent highMarshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        highMarshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire Demon")).hasSize(2);
        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A Temple alone does not replace conjuring with tokens")
    void templeAloneStillConjuresBloodFast() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new ArguelsBloodFast());
        temple.setCard(temple.getCard().getBackFaceCard());
        temple.setTransformed(true);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        marshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Arguel's Blood Fast");
        assertThat(findPermanents(player1, "Temple of Aclazotz")).containsExactly(temple);
        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Temple does not satisfy the condition and only the conjured card transforms")
    void opponentsTempleDoesNotCountAndExistingBloodFastDoesNotTransform() {
        Permanent bloodFast = harness.addToBattlefieldAndReturn(player1, new ArguelsBloodFast());
        Permanent opponentsTemple = harness.addToBattlefieldAndReturn(player2, new ArguelsBloodFast());
        opponentsTemple.setCard(opponentsTemple.getCard().getBackFaceCard());
        opponentsTemple.setTransformed(true);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        marshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Arguel's Blood Fast")).containsExactly(bloodFast);
        assertThat(bloodFast.isTransformed()).isFalse();
        assertThat(findPermanents(player1, "Temple of Aclazotz")).hasSize(1);
        assertThat(findPermanents(player2, "Temple of Aclazotz")).containsExactly(opponentsTemple);
        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The replacement condition checks permanents present at resolution, not at death")
    void gainingBothPermanentsBeforeResolutionCreatesTokens() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        marshal.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.addToBattlefield(player1, new ArguelsBloodFast());
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new ArguelsBloodFast());
        temple.setCard(temple.getCard().getBackFaceCard());
        temple.setTransformed(true);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire Demon")).hasSize(2);
        assertThat(findPermanents(player1, "Arguel's Blood Fast")).hasSize(1);
        assertThat(findPermanents(player1, "Temple of Aclazotz")).containsExactly(temple);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A stolen Marshal conjures for its controller, not its owner")
    void conjuredCardBelongsToDeathTriggerController() {
        HighMarshalArguel card = new HighMarshalArguel();
        card.setOwnerId(player2.getId());
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, card);
        marshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent conjured = findPermanent(player1, "Arguel's Blood Fast");
        assertThat(conjured.getCard().getOwnerId()).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player2, "Arguel's Blood Fast");
        harness.assertInGraveyard(player2, "High Marshal Arguel");
    }
}
