package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanoHellion.class, SerraSphinx.class, Pongify.class})
class VolcanoHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the chosen damage to you and the target creature")
    void dealsChosenDamageToYouAndTargetCreature() {
        Permanent target = castVolcanoHellion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The damage cannot be prevented")
    void damageCannotBePrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        target.setDamagePreventionShield(10);
        castVolcanoHellion(target);

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows choosing zero damage")
    void allowsChoosingZeroDamage() {
        Permanent target = castVolcanoHellion();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Unpaid echo sacrifices Volcano Hellion at its next upkeep")
    void unpaidEchoSacrificesVolcanoHellion() {
        castAndChooseDamage(3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Volcano Hellion");
        harness.assertInGraveyard(player1, "Volcano Hellion");
    }

    @Test
    @DisplayName("Echo uses your current life total as its payment cost")
    void payingEchoUsesCurrentLifeTotal() {
        castAndChooseDamage(3);
        harness.setLife(player1, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Volcano Hellion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Volcano Hellion");
    }

    @Test
    @DisplayName("Echo waits for Volcano Hellion's controller's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndChooseDamage(3);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Volcano Hellion");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Volcano Hellion");
        harness.assertInGraveyard(player1, "Volcano Hellion");
    }

    @Test
    @DisplayName("Echo cost decreases when life is lost after it triggers")
    void echoCostTracksLifeLostInResponse() {
        castAndChooseDamage(0);
        harness.setLife(player1, 10);
        advanceToUpkeep(player1);

        harness.setLife(player1, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Volcano Hellion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Echo cost increases when life is gained after it triggers")
    void echoCostTracksLifeGainedInResponse() {
        castAndChooseDamage(0);
        harness.setLife(player1, 5);
        advanceToUpkeep(player1);

        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Volcano Hellion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An illegal entry target prevents damage but does not remove echo")
    void echoStillTriggersWhenEntryTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        castVolcanoHellionWithTriggerPending(target);
        harness.setHand(player2, List.of(new Pongify()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Volcano Hellion");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Volcano Hellion");
        harness.assertNotOnBattlefield(player1, "Volcano Hellion");
    }

    @Test
    @DisplayName("The entry ability still deals chosen damage after Hellion leaves the battlefield")
    void entryAbilityResolvesWithoutItsSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        castVolcanoHellionWithTriggerPending(target);
        harness.setHand(player2, List.of(new Pongify()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Volcano Hellion"));
        harness.assertInGraveyard(player1, "Volcano Hellion");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);

        harness.assertLife(player1, 17);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private Permanent castVolcanoHellion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        castVolcanoHellion(target);
        return target;
    }

    private void castAndChooseDamage(int damage) {
        castVolcanoHellion();
        harness.handleXValueChosen(player1, damage);
    }

    private void castVolcanoHellion(Permanent target) {
        castVolcanoHellionWithTriggerPending(target);
        resolveAllTriggers();
    }

    private void castVolcanoHellionWithTriggerPending(Permanent target) {
        harness.setHand(player1, List.of(new VolcanoHellion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }
}
