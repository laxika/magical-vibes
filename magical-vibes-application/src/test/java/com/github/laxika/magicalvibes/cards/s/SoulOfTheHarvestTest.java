package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.cards.v.VanguardsShield;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfTheHarvest.class, WanderingWolf.class, ThatcherRevolt.class, VanguardsShield.class})
class SoulOfTheHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger for another entering nontoken creature draws a card")
    void acceptingDrawsCard() {
        addSoul(player1);
        castWolf(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void decliningDrawsNoCard() {
        addSoul(player1);
        castWolf(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger")
    void opponentCreatureDoesNotTrigger() {
        addSoul(player1);
        harness.forceActivePlayer(player2);
        castWolf(player2);

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

    @Test
    @DisplayName("Creature tokens entering do not trigger the draw ability")
    void tokensDoNotTrigger() {
        addSoul(player1);
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger the draw ability")
    void noncreatureDoesNotTrigger() {
        addSoul(player1);
        harness.setHand(player1, List.of(new VanguardsShield()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vanguard's Shield");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A second Soul entering triggers the first Soul exactly once")
    void secondSoulTriggersExistingSoul() {
        addSoul(player1);
        harness.setHand(player1, List.of(new SoulOfTheHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    private void castWolf(Player player) {
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.setHand(player, List.of(new WanderingWolf()));
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve the creature spell; the enters-trigger is queued
        harness.passBothPriorities();
    }

    private void addSoul(Player player) {
        harness.addToBattlefieldAndReturn(player, new SoulOfTheHarvest()).setSummoningSick(false);
    }
}
