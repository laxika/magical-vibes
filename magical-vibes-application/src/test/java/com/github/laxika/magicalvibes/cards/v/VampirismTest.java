package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AnvilOfBogardan;
import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vampirism.class, Breezekeeper.class, AnvilOfBogardan.class})
class VampirismTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 per other creature you control; others get -1/-1")
    void pumpsEnchantedAndShrinksOthers() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vampirism());
        aura.setAttachedTo(host.getId());

        // Host: 4/4 +2/+2 (two other creatures) = 6/6; others: 4/4 -1/-1 = 3/3
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(3);
    }

    @Test
    @DisplayName("On an opponent's creature, counts your creatures and shrinks only yours")
    void onOpponentsCreatureUsesAuraControllersCreatures() {
        Permanent opponentHost = harness.addToBattlefieldAndReturn(player2, new Breezekeeper());
        Permanent ownA = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent ownB = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent opponentOther = harness.addToBattlefieldAndReturn(player2, new Breezekeeper());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vampirism());
        aura.setAttachedTo(opponentHost.getId());

        // Host (opponent): +2/+2 for each of your creatures = 6/6; your creatures -1/-1 = 3/3;
        // opponent's other creature unchanged
        assertThat(gqs.getEffectivePower(gd, opponentHost)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opponentHost)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownA)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownA)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentOther)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentOther)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boosts update when creature count changes")
    void updatesDynamically() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vampirism());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);

        Permanent third = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
    }

    @Test
    @DisplayName("Effects end when the Aura leaves")
    void effectsEndWhenAuraLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vampirism());
        aura.setAttachedTo(host.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void doesNotCountNoncreaturePermanents() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());
        harness.addToBattlefieldAndReturn(player1, new AnvilOfBogardan());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vampirism());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Entering schedules a draw at the next upkeep")
    void schedulesDrawOnEnter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());

        harness.setHand(player1, List.of(new Vampirism()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Breezekeeper());

        harness.setHand(player1, List.of(new Vampirism()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AnvilOfBogardan());
        harness.setHand(player1, List.of(new Vampirism()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
