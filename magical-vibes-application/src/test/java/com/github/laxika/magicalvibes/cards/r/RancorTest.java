package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhituSlinger;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rancor.class, GiantCockroach.class, GhituSlinger.class, GrimMonolith.class})
class RancorTest extends BaseCardTest {

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        harness.setHand(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachRancor(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Rancor());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Rancor
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+0 and has trample")
    void grantsBoostAndTrample() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        attachRancor(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses fall off when Rancor leaves the battlefield")
    void bonusesRemovedWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent aura = attachRancor(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Rancor returns to its owner's hand when the enchanted creature dies")
    void returnsToHandWhenEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        attachRancor(player1, creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GhituSlinger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Rancor");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rancor"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rancor"));
    }

    @Test
    @DisplayName("Rancor returns to its owner's hand when the Aura itself is destroyed")
    void returnsToHandWhenAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent aura = attachRancor(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rancor"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rancor"));
    }
}
