package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfGranite.class, GrizzlyBears.class, FountainOfYouth.class})
class GiftOfGraniteTest extends BaseCardTest {

    @Test
    @DisplayName("Gift of Granite can be cast during the opponent's declare attackers step")
    void canBeCastAtInstantSpeed() {
        Permanent bears = addCreatureReady(player1);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiftOfGranite()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, bears.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Gift of Granite gives the enchanted creature +0/+2")
    void givesEnchantedCreatureToughnessBoost() {
        Permanent bears = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getAttachedTo() != null
                        && permanent.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Gift of Granite stops boosting the creature when it leaves the battlefield")
    void effectStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1);
        Permanent aura = new Permanent(new GiftOfGranite());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gift of Granite fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Gift of Granite cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
