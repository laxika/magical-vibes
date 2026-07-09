package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MerrowReejereyTest extends BaseCardTest {

    // ===== Static effect: buffs own Merfolk =====

    @Test
    @DisplayName("Other Merfolk creatures you control get +1/+1")
    void buffsOwnMerfolk() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player1, new MerfolkSpy());

        Permanent spy = findPermanent(player1, "Merfolk Spy");
        assertThat(gqs.getEffectivePower(gd, spy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Merrow Reejerey does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MerrowReejerey());

        Permanent reejerey = findPermanent(player1, "Merrow Reejerey");
        assertThat(gqs.getEffectivePower(gd, reejerey)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, reejerey)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Trigger: cast a Merfolk spell =====

    @Test
    @DisplayName("Casting a Merfolk spell triggers may ability prompt")
    void merfolkCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting taps an untapped target permanent")
    void acceptTapsUntappedTarget() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();

        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Merrow Reejerey"));

        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting untaps a tapped target permanent")
    void acceptUntapsTappedTarget() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining does not tap or untap anything")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Merrow Reejerey"));
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-Merfolk spell does not trigger Merrow Reejerey")
    void nonMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
