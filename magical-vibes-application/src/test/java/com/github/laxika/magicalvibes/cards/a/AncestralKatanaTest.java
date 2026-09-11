package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralKatana.class, ElvishWarrior.class, GrizzlyBears.class})
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
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger does not fire unless a Samurai or Warrior attacks alone")
    void triggerRequiresSoloSamuraiOrWarrior() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(katana.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger does not fire for a non-Samurai, non-Warrior creature")
    void triggerRequiresSamuraiOrWarrior() {
        Permanent katana = addKatanaReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(katana.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addKatanaReady(Player player) {
        Permanent permanent = new Permanent(new AncestralKatana());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
