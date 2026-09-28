package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BecomeThePilot.class, FountainOfYouth.class, GrizzlyBears.class})
class BecomeThePilotTest extends BaseCardTest {

    @Test
    @DisplayName("Become the Pilot takes control and gives the creature +2/+2")
    void takesControlAndBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Become the Pilot makes a creature unblockable when it attacks a non-owner")
    void cannotBeBlockedWhenAttackingNonOwner() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player1.getId());
        Permanent creature = addCreatureReady(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Become the Pilot allows blocking when the creature attacks its owner")
    void canBeBlockedWhenAttackingOwner() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = addCreatureReady(player2, creatureCard);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Become the Pilot cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncommander creature");
    }
}
