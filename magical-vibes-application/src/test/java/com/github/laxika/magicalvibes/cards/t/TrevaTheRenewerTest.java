package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlazingSpecter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrevaTheRenewer.class, RagingKavu.class, Forest.class, BlazingSpecter.class})
class TrevaTheRenewerTest extends BaseCardTest {

    private int life(Player player) {
        return harness.getGameData().playerLifeTotals.get(player.getId());
    }

    private void resolveCombatToMayPrompt() {
        resolveCombat();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {2}{W} and choosing a color gains life for matching permanents on every battlefield")
    void gainsLifePerPermanentOfChosenColor() {
        Permanent treva = addCreatureReady(player1, new TrevaTheRenewer());
        treva.setAttacking(true);
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player2, new RagingKavu());
        harness.addToBattlefield(player1, new Forest());
        int before = life(player1);

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(life(player1)).isEqualTo(before + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the combat-damage payment gains no life")
    void decliningPaymentGainsNoLife() {
        Permanent treva = addCreatureReady(player1, new TrevaTheRenewer());
        treva.setAttacking(true);
        int before = life(player1);

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(life(player1)).isEqualTo(before);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Combat damage dealt only to a blocker does not trigger Treva")
    void blockedCombatDamageDoesNotTrigger() {
        Permanent treva = addCreatureReady(player1, new TrevaTheRenewer());
        treva.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BlazingSpecter());
        int before = life(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(treva))));
        harness.passBothPriorities();

        assertThat(life(player1)).isEqualTo(before);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
