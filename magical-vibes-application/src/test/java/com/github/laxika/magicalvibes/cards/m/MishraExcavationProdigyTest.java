package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BitterReunion;
import com.github.laxika.magicalvibes.cards.c.ConscriptedInfantry;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishraExcavationProdigy.class, ConscriptedInfantry.class, EnergyRefractor.class, BitterReunion.class})
class MishraExcavationProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card as a cost and draws a card when the ability resolves")
    void discardsThenDraws() {
        addMishra();
        harness.setHand(player1, List.of(new ConscriptedInfantry()));
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateLootAbility(0);

        harness.assertInGraveyard(player1, "Conscripted Infantry");
        harness.assertInHand(player1, "Energy Refractor");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Discarding an artifact adds two red mana")
    void artifactDiscardAddsTwoRedMana() {
        addMishra();
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ConscriptedInfantry()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateLootAbility(0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds red mana at most once each turn for artifact discards")
    void triggersOnlyOnceEachTurn() {
        Permanent mishra = addMishra();
        harness.setHand(player1, List.of(new EnergyRefractor(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ConscriptedInfantry(), new ConscriptedInfantry()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        activateLootAbility(0);
        mishra.untap();
        activateLootAbility(0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The discard is paid immediately and the mana trigger resolves before the draw")
    void discardCostAndManaTriggerPrecedeDraw() {
        Permanent mishra = addMishra();
        EnergyRefractor discarded = new EnergyRefractor();
        ConscriptedInfantry drawn = new ConscriptedInfantry();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(mishra.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A nonartifact discard does not consume the artifact trigger for the turn")
    void nonartifactDiscardDoesNotConsumeTrigger() {
        Permanent mishra = addMishra();
        harness.setHand(player1, List.of(new ConscriptedInfantry(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ConscriptedInfantry(), new ConscriptedInfantry()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateLootAbility(0);
        mishra.untap();
        activateLootAbility(0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The artifact trigger is available again on the opponent's next turn")
    void triggerResetsOnOpponentsTurn() {
        Permanent mishra = addMishra();
        harness.setHand(player1, List.of(new EnergyRefractor(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ConscriptedInfantry(), new ConscriptedInfantry()));
        harness.setLibrary(player2, List.of(new ConscriptedInfantry(), new ConscriptedInfantry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        activateLootAbility(0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        mishra.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        activateLootAbility(0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Haste allows activating the tap ability while summoning sick")
    void activatesWithHaste() {
        Permanent mishra = harness.addToBattlefieldAndReturn(player1, new MishraExcavationProdigy());
        mishra.setSummoningSick(true);
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ConscriptedInfantry()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateLootAbility(0);

        assertThat(mishra.isTapped()).isTrue();
        harness.assertInHand(player1, "Conscripted Infantry");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The draw ability cannot be activated without a card to discard")
    void cannotActivateWithEmptyHand() {
        Permanent mishra = addMishra();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ConscriptedInfantry()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mishra.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact discarded to another card's effect also triggers Mishra")
    void triggersForExternalDiscard() {
        addMishra();

        discardArtifactWithBitterReunion(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Energy Refractor");
    }

    @Test
    @DisplayName("An opponent discarding an artifact does not trigger your Mishra")
    void ignoresOpponentsDiscard() {
        addMishra();

        discardArtifactWithBitterReunion(player2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player2, "Energy Refractor");
    }

    @Test
    @DisplayName("The artifact discard trigger cannot pay the activation's upfront mana cost")
    void cannotPayActivationWithItsOwnDiscardTrigger() {
        Permanent mishra = addMishra();
        EnergyRefractor artifact = new EnergyRefractor();
        harness.setHand(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(new ConscriptedInfantry()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mishra.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMishra() {
        return addCreatureReady(player1, new MishraExcavationProdigy());
    }

    private void activateLootAbility(int discardIndex) {
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, discardIndex);
        resolveAllTriggers();
    }

    private void discardArtifactWithBitterReunion(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new BitterReunion(), new EnergyRefractor()));
        harness.setLibrary(player, List.of(new ConscriptedInfantry(), new ConscriptedInfantry()));
        harness.addMana(player, ManaColor.RED, 2);
        harness.castEnchantment(player, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player, true);
        harness.handleCardChosen(player, 0);
        resolveAllTriggers();
    }
}
