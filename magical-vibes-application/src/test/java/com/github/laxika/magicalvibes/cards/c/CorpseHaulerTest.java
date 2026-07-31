package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorpseHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice cost is paid on activation")
    void sacrificeIsPaidOnActivation() {
        addHaulerToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Corpse Hauler");
        harness.assertInGraveyard(player1, "Corpse Hauler");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Returns a creature card from graveyard to hand")
    void returnsCreatureToHand() {
        addHaulerToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses a specific creature when several are in the graveyard")
    void choosesSpecificCreature() {
        addHaulerToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Corpse Hauler itself is not a legal choice — the ability returns another creature card")
    void cannotReturnItself() {
        addHaulerToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Index 1 is the sacrificed Corpse Hauler — excluded by "another"
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolves with no effect when the sacrificed Corpse Hauler is the only creature card")
    void noEffectWhenOnlySelfInGraveyard() {
        addHaulerToBattlefield(player1);
        harness.setGraveyard(player1, List.of());
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Corpse Hauler");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addHaulerToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addHaulerToBattlefield(Player player) {
        Permanent hauler = new Permanent(new CorpseHauler());
        hauler.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(hauler);
        return hauler;
    }
}
