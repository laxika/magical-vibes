package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlliesAtLast.class, AvatarEnthusiasts.class, ColossalDreadmaw.class, GrizzlyBears.class,
        LlanowarElves.class})
class AlliesAtLastTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures each deal their power to an opponent creature")
    void twoTargetCreaturesDealTheirPower() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        cast(List.of(victim.getId(), bear.getId(), elf.getId()), 3);

        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The source creature group is optional")
    void allowsOneSource() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        cast(List.of(victim.getId(), bear.getId()), 3);

        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Affinity for Allies reduces the generic cost")
    void affinityForAlliesReducesGenericCost() {
        harness.addToBattlefield(player1, new AvatarEnthusiasts());
        harness.addToBattlefield(player1, new AvatarEnthusiasts());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        cast(List.of(victim.getId(), bear.getId()), 1);

        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Affinity counts only Allies controlled by the spell's controller")
    void affinityCountsOnlyControlledAllies() {
        harness.addToBattlefield(player1, new AvatarEnthusiasts());
        harness.addToBattlefield(player2, new AvatarEnthusiasts());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new AlliesAtLast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(victim.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Only creatures you control can be chosen as sources")
    void sourcesMustBeControlled() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new AlliesAtLast()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(victim.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The damage target must be a creature an opponent controls")
    void victimMustBeOpponentCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new AlliesAtLast()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownCreature.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds, int greenMana) {
        harness.setHand(player1, List.of(new AlliesAtLast()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.castInstant(player1, 0, targetIds);
    }
}
