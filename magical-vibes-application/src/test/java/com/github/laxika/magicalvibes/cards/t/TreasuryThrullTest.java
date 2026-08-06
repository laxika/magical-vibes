package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreasuryThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns a creature card from the graveyard to hand")
    void attackReturnsCreatureCardToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addReadyThrull(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Artifact and enchantment cards are also legal returns")
    void artifactAndEnchantmentAreLegal() {
        harness.setGraveyard(player1, List.of(new Ornithopter(), new HolyStrength()));
        addReadyThrull(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Instants and lands in the graveyard are not legal returns")
    void nonMatchingCardsAreSkipped() {
        harness.setGraveyard(player1, List.of(new HolyDay(), new Plains()));
        addReadyThrull(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the graveyard untouched")
    void decliningLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addReadyThrull(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Extort drains the opponent for 1 when the {W/B} is paid")
    void extortDrainsOpponent() {
        harness.addToBattlefield(player1, new TreasuryThrull());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining extort leaves life totals unchanged")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new TreasuryThrull());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addReadyThrull(Player player) {
        Permanent perm = new Permanent(new TreasuryThrull());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
