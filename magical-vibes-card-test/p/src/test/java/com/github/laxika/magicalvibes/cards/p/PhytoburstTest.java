package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GruulCluestone;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Phytoburst.class, KraulWarrior.class, GruulCluestone.class})
class PhytoburstTest extends BaseCardTest {

    @Test
    @DisplayName("Phytoburst gives the target creature +5/+5")
    void boostsTarget() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Kraul Warrior");
        harness.castAndResolveSorcery(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(7);
        assertThat(bear.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Phytoburst's boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Kraul Warrior");
        harness.castAndResolveSorcery(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Phytoburst can boost an opponent's creature without boosting another creature")
    void boostsOpponentCreatureOnly() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.addToBattlefield(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Kraul Warrior"));

        Permanent target = findPermanent(player2, "Kraul Warrior");
        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
        Permanent other = findPermanent(player1, "Kraul Warrior");
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Phytobursts stack and both boosts expire at end of turn")
    void multipleBoostsStackAndExpire() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new Phytoburst(), new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        UUID targetId = harness.getPermanentId(player1, "Kraul Warrior");

        harness.castAndResolveSorcery(player1, 0, targetId);
        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent target = findPermanent(player1, "Kraul Warrior");
        assertThat(target.getEffectivePower()).isEqualTo(12);
        assertThat(target.getEffectiveToughness()).isEqualTo(12);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Phytoburst cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.addToBattlefield(player1, new GruulCluestone());
        harness.setHand(player1, List.of(new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID fountainId = harness.getPermanentId(player1, "Gruul Cluestone");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
