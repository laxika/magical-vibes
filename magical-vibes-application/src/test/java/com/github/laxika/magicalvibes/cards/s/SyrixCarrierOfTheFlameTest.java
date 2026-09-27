package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LingeringSouls;
import com.github.laxika.magicalvibes.cards.p.PhoenixChick;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
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

@CardUsed({SyrixCarrierOfTheFlame.class, WorldheartPhoenix.class, LingeringSouls.class,
        PhoenixChick.class, Shock.class})
class SyrixCarrierOfTheFlameTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card leaving your graveyard enables the end-step Phoenix damage")
    void creatureCardLeavingGraveyardTriggersEndStepDamage() {
        Permanent syrix = harness.addToBattlefieldAndReturn(player1, new SyrixCarrierOfTheFlame());
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        addManaForWorldheartPhoenix();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities(); // resolve Worldheart Phoenix and its enter-the-battlefield effect

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, syrix.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A noncreature card leaving your graveyard does not enable the end-step damage")
    void noncreatureCardLeavingGraveyardDoesNotTriggerEndStepDamage() {
        harness.addToBattlefield(player1, new SyrixCarrierOfTheFlame());
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Another Phoenix dying lets you cast Syrix from your graveyard for free")
    void phoenixDeathCastsSyrixFromGraveyard() {
        harness.setGraveyard(player1, List.of(new SyrixCarrierOfTheFlame()));
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new PhoenixChick());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, phoenix.getId());
        harness.passBothPriorities(); // resolve Shock and put Phoenix Chick into the graveyard
        harness.passBothPriorities(); // resolve Syrix's triggered ability

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Syrix, Carrier of the Flame");
        harness.assertInGraveyard(player1, "Phoenix Chick");
    }

    private void addManaForWorldheartPhoenix() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
