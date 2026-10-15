package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KamiOfTerribleSecrets;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.n.NezumiBladeblesser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfHiddenCruelty.class, HondenOfSeeingWinds.class, NezumiBladeblesser.class,
        KamiOfTerribleSecrets.class, CoilingStalker.class, GoShintaiOfLostWisdom.class})
class GoShintaiOfHiddenCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} destroys a target creature whose toughness is at most the Shrine count")
    void paysToDestroyCreatureWithinShrineCount() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        Permanent bladeblesser = addCreatureReady(player2, new NezumiBladeblesser());
        Permanent kami = addCreatureReady(player2, new KamiOfTerribleSecrets());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                        findPermanent(player1, "Go-Shintai of Hidden Cruelty").getId(), bladeblesser.getId())
                .doesNotContain(kami.getId());
        harness.handlePermanentChosen(player1, bladeblesser.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nezumi Bladeblesser");
        harness.assertOnBattlefield(player2, "Kami of Terrible Secrets");
    }

    @Test
    @DisplayName("Declining the payment does not destroy a creature")
    void declinesPayment() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent bladeblesser = addCreatureReady(player2, new NezumiBladeblesser());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bladeblesser);
    }

    @Test
    @DisplayName("A creature above the Shrine count is not a legal target")
    void creatureAboveShrineCountCannotBeTargeted() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent bladeblesser = addCreatureReady(player2, new NezumiBladeblesser());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bladeblesser);
    }

    @Test
    @DisplayName("Payment creates a separate trigger that players can respond to before destruction")
    void destructionUsesSeparateReflexiveTrigger() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent target = addCreatureReady(player2, new CoilingStalker());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, target.getId());
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(target.getId());
            harness.passBothPriorities();
        });

        harness.assertInGraveyard(player2, "Coiling Stalker");
    }

    @Test
    @DisplayName("Opponent's Shrines do not increase the toughness limit")
    void opponentsShrinesDoNotCount() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        harness.addToBattlefield(player2, new GoShintaiOfLostWisdom());
        Permanent target = addCreatureReady(player2, new NezumiBladeblesser());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("A legal creature survives when the controller cannot pay")
    void cannotDestroyWithoutPaying() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent target = addCreatureReady(player2, new CoilingStalker());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The destruction ability can target a creature you control")
    void canDestroyOwnCreature() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent target = addCreatureReady(player1, new CoilingStalker());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });

        harness.assertInGraveyard(player1, "Coiling Stalker");
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        addCreatureReady(player2, new CoilingStalker());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
