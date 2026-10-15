package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.j.JawboneSkulkin;
import com.github.laxika.magicalvibes.cards.w.WoodlurkerMimic;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NettleSentinel.class, JawboneSkulkin.class, WoodlurkerMimic.class})
class NettleSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Nettle Sentinel does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        sentinel.tap();

        advanceToUpkeep(player1);

        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap choice waits until the green-spell trigger resolves")
    void greenSpellTriggersMayPrompt() {
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting untaps a tapped Nettle Sentinel")
    void acceptUntapsSentinel() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        sentinel.tap();

        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves Nettle Sentinel tapped")
    void declineLeavesTapped() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        sentinel.tap();

        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Nettle Sentinel"));
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-green spell does not trigger Nettle Sentinel")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.setHand(player1, List.of(new JawboneSkulkin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("An opponent's green spell does not trigger Nettle Sentinel")
    void opponentsGreenSpellDoesNotTrigger() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        sentinel.tap();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NettleSentinel()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A black-green hybrid spell triggers even when paid entirely with black mana")
    void hybridSpellPaidWithBlackTriggers() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        sentinel.tap();
        harness.setHand(player1, List.of(new WoodlurkerMimic()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nettle Sentinel does not trigger for its own casting")
    void doesNotTriggerForItsOwnCasting() {
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nettle Sentinel");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
