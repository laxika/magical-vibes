package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cagemail.class, KrosanVerge.class, SuntailHawk.class})
class CagemailTest extends BaseCardTest {

    @Test
    @DisplayName("Cagemail gives the enchanted creature +2/+2")
    void boostsEnchantedCreature() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cagemail());
        aura.setAttachedTo(hawk.getId());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cagemail prevents the enchanted creature from attacking")
    void preventsEnchantedCreatureFromAttacking() {
        addCreatureReady(player1, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Cagemail());
        aura.setAttachedTo(gd.playerBattlefields.get(player1.getId()).get(0).getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cagemail still allows the enchanted creature to block")
    void allowsEnchantedCreatureToBlock() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cagemail());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Removing Cagemail restores the enchanted creature's stats and attack ability")
    void effectsStopWhenRemoved() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Cagemail());
        aura.setAttachedTo(hawk.getId());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(hawk.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Casting Cagemail attaches it to a target creature")
    void castingCagemailAttachesToTargetCreature() {
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, hawk.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Cagemail
                        && hawk.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cagemail cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
