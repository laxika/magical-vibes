package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.cards.r.RiftBolt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.e.EmptyTheWarrens;
import com.github.laxika.magicalvibes.cards.f.Fortify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiMageOfZhalfir.class, BenalishCavalry.class, Fortify.class, EmptyTheWarrens.class,
        SuddenSpoiling.class, DryadArbor.class, RiftBolt.class})
class TeferiMageOfZhalfirTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can cast creature spells at instant speed")
    void controllerCanCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent cannot use Teferi to cast a creature at instant speed")
    void opponentCannotCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BenalishCavalry()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent cannot cast an instant outside their own sorcery timing")
    void opponentCannotCastInstantOutsideSorceryTiming() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Fortify()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature flash grant does not apply to noncreature sorceries")
    void creatureFlashGrantDoesNotApplyToNoncreatureSorceries() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new EmptyTheWarrens()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent can cast a spell during their own sorcery timing")
    void opponentCanCastDuringOwnSorceryTiming() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Fortify()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void teferiCanBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new TeferiMageOfZhalfir()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teferi, Mage of Zhalfir");
    }

    @Test
    void opponentCannotRespondDuringTheirOwnMainPhase() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BenalishCavalry(), new Fortify()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opposingTeferiRestrictionOverridesCreatureFlash() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.addToBattlefield(player2, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void losingAbilitiesRemovesOpponentCastingRestriction() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SuddenSpoiling(), new Fortify()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void losingAbilitiesRemovesCreatureFlashGrant() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.setHand(player1, List.of(new SuddenSpoiling(), new BenalishCavalry()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void ownedCreatureLandCanBePlayedDuringOwnCombat() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new DryadArbor()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Dryad Arbor");
    }

    @Test
    void opponentCannotCastSuspendedSpellWhenLastCounterIsRemoved() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        RiftBolt card = new RiftBolt();
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateHandAbility(player2, 0, null);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(card.getId()));
    }
}
