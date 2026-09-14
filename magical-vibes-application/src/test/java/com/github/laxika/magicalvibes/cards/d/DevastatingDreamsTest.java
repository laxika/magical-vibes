package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.c.CephalidAristocrat;
import com.github.laxika.magicalvibes.cards.t.TaintedField;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevastatingDreams.class, CabalCoffers.class, CephalidAristocrat.class, TaintedField.class})
class DevastatingDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Randomly discards X, sacrifices X lands, and deals X damage to each creature")
    void resolvesAllEffects() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        Permanent opponentAristocrat = harness.addToBattlefieldAndReturn(player2, new CephalidAristocrat());
        harness.addToBattlefield(player1, new TaintedField());
        harness.addToBattlefield(player2, new CabalCoffers());
        harness.setHand(player1, List.of(new DevastatingDreams(), new TaintedField(), new CabalCoffers()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(aristocrat.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentAristocrat.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Tainted Field");
        harness.assertNotOnBattlefield(player2, "Cabal Coffers");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Devastating Dreams");
    }

    @Test
    @DisplayName("Each player chooses which lands to sacrifice")
    void eachPlayerChoosesLands() {
        harness.addToBattlefield(player1, new TaintedField());
        harness.addToBattlefield(player1, new CabalCoffers());
        harness.addToBattlefield(player2, new TaintedField());
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setHand(player1, List.of(new DevastatingDreams(), new TaintedField()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Tainted Field")));

        harness.assertOnBattlefield(player1, "Cabal Coffers");
        harness.assertNotOnBattlefield(player1, "Tainted Field");
        harness.assertNotOnBattlefield(player2, "Tainted Field");
        assertThat(aristocrat.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each player chooses two lands before the simultaneous sacrifice at X=2")
    void eachPlayerChoosesMultipleLandsBeforeSacrifice() {
        Permanent player1TaintedField = harness.addToBattlefieldAndReturn(player1, new TaintedField());
        Permanent player1CabalCoffers = harness.addToBattlefieldAndReturn(player1, new CabalCoffers());
        Permanent player1OtherTaintedField = harness.addToBattlefieldAndReturn(player1, new TaintedField());
        Permanent player2TaintedField = harness.addToBattlefieldAndReturn(player2, new TaintedField());
        Permanent player2CabalCoffers = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());
        Permanent player2OtherTaintedField = harness.addToBattlefieldAndReturn(player2, new TaintedField());
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        Permanent opponentAristocrat = harness.addToBattlefieldAndReturn(player2, new CephalidAristocrat());
        harness.setHand(player1, List.of(
                new DevastatingDreams(), new TaintedField(), new CabalCoffers()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(2);
        assertThat(player1Choice.validIds())
                .containsExactlyInAnyOrder(
                        player1TaintedField.getId(), player1CabalCoffers.getId(),
                        player1OtherTaintedField.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(player1TaintedField.getId(), player1CabalCoffers.getId()));

        assertThat(findPermanents(player1, "Tainted Field")).hasSize(2);
        assertThat(findPermanents(player1, "Cabal Coffers")).hasSize(1);

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(2);
        assertThat(player2Choice.validIds())
                .containsExactlyInAnyOrder(
                        player2TaintedField.getId(), player2CabalCoffers.getId(),
                        player2OtherTaintedField.getId());

        harness.handleMultiplePermanentsChosen(player2,
                List.of(player2TaintedField.getId(), player2CabalCoffers.getId()));

        harness.assertOnBattlefield(player1, "Tainted Field");
        harness.assertNotOnBattlefield(player1, "Cabal Coffers");
        harness.assertOnBattlefield(player2, "Tainted Field");
        harness.assertNotOnBattlefield(player2, "Cabal Coffers");
        assertThat(aristocrat.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentAristocrat.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting is rejected when the hand cannot cover random X discards")
    void cannotCastWithoutEnoughCardsToDiscard() {
        harness.setHand(player1, List.of(new DevastatingDreams()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X=0 does nothing")
    void zeroXDoesNothing() {
        harness.addToBattlefield(player1, new TaintedField());
        harness.addToBattlefield(player2, new CabalCoffers());
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setHand(player1, List.of(new DevastatingDreams(), new TaintedField()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tainted Field");
        harness.assertOnBattlefield(player2, "Cabal Coffers");
        assertThat(aristocrat.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Tainted Field");
    }
}
