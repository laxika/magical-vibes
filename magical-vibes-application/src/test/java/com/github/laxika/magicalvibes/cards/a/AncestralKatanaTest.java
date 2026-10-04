package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SunbladeSamurai;
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

@CardUsed({AncestralKatana.class, ElvishWarrior.class, GrizzlyBears.class, SunbladeSamurai.class})
class AncestralKatanaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = addKatanaReady(player1);
        katana.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking alone with a Samurai or Warrior and paying attaches the Katana")
    void attackingAloneWithSamuraiOrWarriorAttachesOnPayment() {
        Permanent katana = addKatanaReady(player1);
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(katana.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(katana.getAttachedTo()).isEqualTo(warrior.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger payment leaves the Katana unattached")
    void decliningAttackTriggerPaymentLeavesKatanaUnattached() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The trigger does not fire unless a Samurai or Warrior attacks alone")
    void triggerRequiresSoloSamuraiOrWarrior() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The trigger does not fire for a non-Samurai, non-Warrior creature")
    void triggerRequiresSamuraiOrWarrior() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    void equipPaysThreeManaAndMovesTheBoost() {
        Permanent katana = addKatanaReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        katana.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, second.getId());

        assertThat(katana.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(katana.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void samuraiAttackCreatesOneNonTargetingTrigger() {
        Permanent katana = addKatanaReady(player1);
        Permanent samurai = addCreatureReady(player1, new SunbladeSamurai());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(katana.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(katana.getAttachedTo()).isEqualTo(samurai.getId());
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(5);
    }

    @Test
    void opponentsSoloWarriorDoesNotTriggerKatana() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player2, new ElvishWarrior());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    void decliningPaymentKeepsExistingAttachmentAndMana() {
        Permanent katana = addKatanaReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ElvishWarrior());
        katana.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(katana.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingWithTwoWarriorsCreatesNoTrigger() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(katana.getAttachedTo()).isNull();
    }

    private Permanent addKatanaReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AncestralKatana());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
