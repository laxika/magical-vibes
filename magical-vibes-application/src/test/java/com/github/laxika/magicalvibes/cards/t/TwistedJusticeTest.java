package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DriftOfPhantasms;
import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwistedJustice.class, Forest.class, BorosSwiftblade.class, Watchwolf.class, DriftOfPhantasms.class})
class TwistedJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player chooses a creature, and the controller draws cards equal to its power")
    void targetPlayerChoosesCreatureAndControllerDrawsItsPower() {
        Permanent swiftblade = harness.addToBattlefieldAndReturn(player2, new BorosSwiftblade());
        Permanent watchwolf = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(swiftblade.getId(), watchwolf.getId());

        harness.handlePermanentChosen(player2, watchwolf.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
        harness.assertInGraveyard(player2, "Watchwolf");
        harness.assertOnBattlefield(player2, "Boros Swiftblade");
    }

    @Test
    @DisplayName("With one creature, the target player sacrifices it automatically")
    void automaticallySacrificesOnlyCreature() {
        harness.addToBattlefield(player2, new BorosSwiftblade());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Boros Swiftblade");
    }

    @Test
    @DisplayName("The controller may target themselves")
    void controllerMayBeTargeted() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Watchwolf");
    }

    @Test
    @DisplayName("A target player with no creatures causes no draw")
    void noCreatureCausesNoDraw() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draw count includes counters on the creature before it is sacrificed")
    void drawsUsingPowerBeforeSacrifice() {
        Permanent swiftblade = harness.addToBattlefieldAndReturn(player2, new BorosSwiftblade());
        swiftblade.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Boros Swiftblade");
        harness.assertNotOnBattlefield(player2, "Boros Swiftblade");
    }

    @Test
    @DisplayName("The target may choose a zero-power creature and the caster draws nothing")
    void choosingZeroPowerCreatureCausesNoDraw() {
        Permanent drift = harness.addToBattlefieldAndReturn(player2, new DriftOfPhantasms());
        harness.addToBattlefield(player2, new Watchwolf());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TwistedJustice()));
        addManaForTwistedJustice();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, drift.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Drift of Phantasms");
        harness.assertOnBattlefield(player2, "Watchwolf");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addManaForTwistedJustice() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
