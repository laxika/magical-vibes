package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({MurderousCompulsion.class, GrizzlyBears.class, RavensCrime.class, Forest.class})
class MurderousCompulsionTest extends BaseCardTest {

    private Permanent addTappedBears(Player owner) {
        Permanent bears = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        bears.tap();
        return bears;
    }

    /** Force player1 to discard Murderous Compulsion via Raven's Crime from player2. */
    private MurderousCompulsion discardViaRavensCrime() {
        MurderousCompulsion compulsion = new MurderousCompulsion();
        harness.setHand(player1, List.of(compulsion));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return compulsion;
    }

    @Test
    @DisplayName("Casting destroys target tapped creature")
    void destroysTargetTappedCreature() {
        Permanent target = addTappedBears(player2);
        harness.setHand(player1, List.of(new MurderousCompulsion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MurderousCompulsion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Can destroy a tapped creature controlled by the caster")
    void destroysOwnTappedCreature() {
        Permanent target = addTappedBears(player1);
        harness.setHand(player1, List.of(new MurderousCompulsion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Murderous Compulsion");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        harness.setHand(player1, List.of(new MurderousCompulsion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("A target that untaps before resolution survives")
    void untappedTargetSurvivesResolution() {
        Permanent target = addTappedBears(player2);
        harness.setHand(player1, List.of(new MurderousCompulsion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Murderous Compulsion");
    }

    @Test
    @DisplayName("Madness with no legal target puts the card in the graveyard without paying mana")
    void madnessWithoutLegalTargetGoesToGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        MurderousCompulsion compulsion = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(compulsion.getId()));
        harness.assertInGraveyard(player1, "Murderous Compulsion");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Madness with insufficient mana puts the card in the graveyard")
    void madnessWithoutEnoughManaGoesToGraveyard() {
        addTappedBears(player2);
        MurderousCompulsion compulsion = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(compulsion.getId()));
        harness.assertInGraveyard(player1, "Murderous Compulsion");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding Murderous Compulsion exiles it and offers madness cast")
    void discardTriggersMadness() {
        MurderousCompulsion compulsion = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(compulsion.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness cast puts the card into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        MurderousCompulsion compulsion = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(compulsion.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(compulsion.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {1}{B} and destroys target tapped creature")
    void acceptingMadnessDestroysTappedCreature() {
        Permanent target = addTappedBears(player2);
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Murderous Compulsion");
    }
}
