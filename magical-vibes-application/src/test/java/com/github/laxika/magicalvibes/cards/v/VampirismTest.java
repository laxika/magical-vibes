package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VampirismTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 per other creature you control; others get -1/-1")
    void pumpsEnchantedAndShrinksOthers() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        Permanent third = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);
        gd.playerBattlefields.get(player1.getId()).add(third);

        Permanent aura = new Permanent(new Vampirism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Host: 2/2 +2/+2 (two other creatures) = 4/4; others: 2/2 -1/-1 = 1/1
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(1);
    }

    @Test
    @DisplayName("On an opponent's creature, counts your creatures and shrinks only yours")
    void onOpponentsCreatureUsesAuraControllersCreatures() {
        Permanent opponentHost = new Permanent(new GrizzlyBears());
        Permanent ownA = new Permanent(new GrizzlyBears());
        Permanent ownB = new Permanent(new GrizzlyBears());
        Permanent opponentOther = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentHost);
        gd.playerBattlefields.get(player2.getId()).add(opponentOther);
        gd.playerBattlefields.get(player1.getId()).add(ownA);
        gd.playerBattlefields.get(player1.getId()).add(ownB);

        Permanent aura = new Permanent(new Vampirism());
        aura.setAttachedTo(opponentHost.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Host (opponent): +2/+2 for each of your creatures = 4/4; your creatures -1/-1 = 1/1;
        // opponent's other creature unchanged
        assertThat(gqs.getEffectivePower(gd, opponentHost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentHost)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownA)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownA)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentOther)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentOther)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts update when creature count changes")
    void updatesDynamically() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);

        Permanent aura = new Permanent(new Vampirism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);

        Permanent third = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(third);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(1);
    }

    @Test
    @DisplayName("Effects end when the Aura leaves")
    void effectsEndWhenAuraLeaves() {
        Permanent host = new Permanent(new GrizzlyBears());
        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(host);
        gd.playerBattlefields.get(player1.getId()).add(other);

        Permanent aura = new Permanent(new Vampirism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering schedules a draw at the next upkeep")
    void schedulesDrawOnEnter() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new Vampirism()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new Vampirism()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Vampirism()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
