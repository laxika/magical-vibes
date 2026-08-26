package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BartzAndBoko.class, AvenInitiate.class, AirElemental.class, GrizzlyBears.class})
class BartzAndBokoTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Birds reduces Bartz and Boko's generic mana cost")
    void affinityForBirdsReducesGenericCost() {
        harness.addToBattlefield(player1, new AvenInitiate());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Other Birds deal their power as damage to the targeted opposing creature")
    void otherBirdsDealPowerDamageToTargetCreature() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenInitiate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = target.getId();
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(gqs.getEffectivePower(gd, bird));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The ETB cannot target a creature controlled by Bartz and Boko's controller")
    void etbCannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }
}
