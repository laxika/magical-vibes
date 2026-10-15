package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hammerhand.class, RuneclawBear.class})
class HammerhandTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has haste")
    void enchantedCreatureBoostedAndHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Hammerhand());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the boost and haste when Hammerhand leaves")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Hammerhand());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("ETB can't-block hits the targeted creature, not the enchanted creature")
    void etbCantBlockHitsTargetNotEnchanted() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent opponentBlocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, mine.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBlocker.getId());
        harness.passBothPriorities();

        assertThat(opponentBlocker.isCantBlockThisTurn()).isTrue();
        assertThat(mine.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hammerhand")
                        && mine.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Hammerhand can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentBears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mine.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();
        assertThat(mine.isCantBlockThisTurn()).isTrue();
    }
    @Test
    @DisplayName("The ETB target cannot be supplied as an additional Aura spell target")
    void rejectsPrematureEtbTarget() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(host.getId(), blocker.getId())))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enchanted creature may also be the ETB target")
    void canTargetEnchantedCreatureWithTrigger() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, host.getId());
        harness.passBothPriorities();

        assertThat(host.isCantBlockThisTurn()).isTrue();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ETB can target a creature that entered after the Aura was cast")
    void triggerChoosesTargetAfterAuraEnters() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, host.getId());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blocker.getId());
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof Hammerhand).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(host.isCantBlockThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isFalse();
    }
}
