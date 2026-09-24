package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulSliver.class, BonescytheSliver.class, GrizzlyBears.class, Shock.class})
class SpitefulSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver you control deals the damage it was dealt to a target player")
    void sliverReflectsDamageToTargetPlayer() {
        Permanent spitefulSliver = harness.addToBattlefieldAndReturn(player1, new SpitefulSliver());
        dealDamageTo(player2, spitefulSliver);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The ability is granted to other Slivers you control")
    void grantsAbilityToOtherControlledSlivers() {
        harness.addToBattlefield(player1, new SpitefulSliver());
        Permanent otherSliver = harness.addToBattlefieldAndReturn(player1, new BonescytheSliver());
        dealDamageTo(player2, otherSliver);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The ability is not granted to non-Slivers or an opponent's Slivers")
    void onlyAffectsSliversYouControl() {
        harness.addToBattlefield(player1, new SpitefulSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSliver = harness.addToBattlefieldAndReturn(player2, new BonescytheSliver());

        dealDamageTo(player2, nonSliver);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        dealDamageTo(player1, opponentSliver);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void dealDamageTo(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
