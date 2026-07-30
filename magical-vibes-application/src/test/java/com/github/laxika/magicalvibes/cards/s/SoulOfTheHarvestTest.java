package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulOfTheHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger for another entering nontoken creature draws a card")
    void acceptingDrawsCard() {
        addSoul(player1);
        castBears(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void decliningDrawsNoCard() {
        addSoul(player1);
        castBears(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger")
    void opponentCreatureDoesNotTrigger() {
        addSoul(player1);
        harness.forceActivePlayer(player2);
        castBears(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Soul of the Harvest entering itself does not trigger its own ability")
    void ownEntryDoesNotTrigger() {
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player1, List.of(new SoulOfTheHarvest()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    private void castBears(Player player) {
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve the creature spell; the enters-trigger is queued
        harness.passBothPriorities();
    }

    private void addSoul(Player player) {
        Permanent perm = new Permanent(new SoulOfTheHarvest());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
