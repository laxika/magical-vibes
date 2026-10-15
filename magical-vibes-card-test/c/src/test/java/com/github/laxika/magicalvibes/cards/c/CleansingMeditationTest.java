package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.h.Hypochondria;
import com.github.laxika.magicalvibes.cards.s.StrengthOfIsolation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleansingMeditation.class, Hypochondria.class, AvenTrooper.class, StrengthOfIsolation.class})
class CleansingMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all enchantments without threshold")
    void destroysAllEnchantmentsWithoutThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Hypochondria());
        harness.addToBattlefield(player2, new Hypochondria());
        harness.addToBattlefield(player1, new AvenTrooper());
        harness.addToBattlefield(player2, new AvenTrooper());

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Hypochondria");
        harness.assertNotOnBattlefield(player2, "Hypochondria");
        harness.assertOnBattlefield(player1, "Aven Trooper");
        harness.assertOnBattlefield(player2, "Aven Trooper");
    }

    @Test
    @DisplayName("Threshold returns only enchantments destroyed into your graveyard")
    void thresholdReturnsOnlyEnchantmentsDestroyedIntoYourGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Hypochondria());
        Card previousEnchantment = new Hypochondria();
        harness.setGraveyard(player1, List.of(
                previousEnchantment,
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper()));

        castCleansingMeditation();

        harness.assertOnBattlefield(player1, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(previousEnchantment)
                .hasSize(8);
    }

    @Test
    @DisplayName("Threshold is checked before the destruction adds cards to the graveyard")
    void thresholdIsCheckedBeforeDestruction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card enchantment = new Hypochondria();
        harness.addToBattlefield(player1, enchantment);
        harness.setGraveyard(player1, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper()));

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchantment)
                .hasSize(8);
    }

    @Test
    @DisplayName("Threshold does not return enchantments put into an opponent's graveyard")
    void thresholdDoesNotReturnOpponentsEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Hypochondria());
        Card opponentEnchantment = new Hypochondria();
        harness.addToBattlefield(player2, opponentEnchantment);
        harness.setGraveyard(player1, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper()));

        castCleansingMeditation();

        harness.assertOnBattlefield(player1, "Hypochondria");
        harness.assertNotOnBattlefield(player2, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentEnchantment);
    }

    @Test
    @DisplayName("Threshold returns all destroyed enchantments according to ownership, not control")
    void thresholdReturnsEnchantmentsAccordingToOwnership() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card ownEnchantment = new Hypochondria();
        ownEnchantment.setOwnerId(player1.getId());
        Card stolenOwnEnchantment = new Hypochondria();
        stolenOwnEnchantment.setOwnerId(player1.getId());
        Card stolenOpponentEnchantment = new Hypochondria();
        stolenOpponentEnchantment.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, ownEnchantment);
        harness.addToBattlefield(player2, stolenOwnEnchantment);
        harness.addToBattlefield(player1, stolenOpponentEnchantment);
        harness.setGraveyard(player1, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper()));

        castCleansingMeditation();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactlyInAnyOrder(ownEnchantment, stolenOwnEnchantment);
        harness.assertNotOnBattlefield(player2, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(ownEnchantment, stolenOwnEnchantment, stolenOpponentEnchantment);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(stolenOpponentEnchantment);
    }

    @Test
    @DisplayName("Cards in the opponent's graveyard do not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card enchantment = new Hypochondria();
        harness.addToBattlefield(player1, enchantment);
        harness.setGraveyard(player2, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper()));

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment);
    }

    @Test
    @DisplayName("Threshold returns a destroyed Aura attached to a chosen legal creature")
    void thresholdReturnsAuraAttachedToChosenCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var first = harness.addToBattlefieldAndReturn(player1, new AvenTrooper());
        var second = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        Card aura = new StrengthOfIsolation();
        var auraPermanent = harness.addToBattlefieldAndReturn(player1, aura);
        auraPermanent.setAttachedTo(first.getId());
        harness.setGraveyard(player1, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper()));

        castCleansingMeditation();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());

        harness.assertOnBattlefield(player1, "Strength of Isolation");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(aura.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getAttachedTo()).isEqualTo(second.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    private void castCleansingMeditation() {
        harness.castFromHand(player1, new CleansingMeditation(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
