package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpitFlameTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        prepareMain(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpitFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A Dragon entering lets you pay {R} to return Spit Flame to hand")
    void dragonEntersPayReturnsToHand() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);

        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{R}");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(spitFlame.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(spitFlame.getId()));
    }

    @Test
    @DisplayName("Declining the Dragon trigger keeps Spit Flame in the graveyard")
    void declineKeepsSpitFlameInGraveyard() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);

        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(spitFlame.getId()));
    }

    @Test
    @DisplayName("A non-Dragon creature entering does not trigger")
    void nonDragonDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SpitFlame()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A Dragon an opponent controls entering does not trigger")
    void opponentDragonDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SpitFlame()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new DragonEgg()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Cannot return Spit Flame without {R}")
    void cannotReturnWithoutRedMana() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);

        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(spitFlame.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(spitFlame.getId()));
    }
}
