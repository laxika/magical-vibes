package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UnquenchableFury.class, GrizzlyBears.class, FountainOfYouth.class})
class UnquenchableFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature deals damage equal to the defending player's hand size")
    void attacksDealDamageEqualToDefendingPlayersHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(attacker);

        declareAttackers(player1, List.of(0));
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Unquenchable Fury returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachAura(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Unquenchable Fury");
        harness.assertNotInGraveyard(player1, "Unquenchable Fury");
        harness.assertNotOnBattlefield(player1, "Unquenchable Fury");
    }

    @Test
    @DisplayName("Unquenchable Fury cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new UnquenchableFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnquenchableFury());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
