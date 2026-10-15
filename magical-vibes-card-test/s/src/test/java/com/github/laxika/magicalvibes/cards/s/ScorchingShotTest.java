package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalRattlewurm;
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

@CardUsed({ScorchingShot.class, GrizzlyBears.class, ColossalRattlewurm.class, SpinewoodsArmadillo.class})
class ScorchingShotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target creature")
    void dealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScorchingShot()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ScorchingShot()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Five damage is lethal to a creature with five toughness")
    void killsCreatureWithExactlyFiveToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        harness.setHand(player1, List.of(new ScorchingShot()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Colossal Rattlewurm");
        harness.assertInGraveyard(player2, "Colossal Rattlewurm");
        harness.assertInGraveyard(player1, "Scorching Shot");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target a friendly creature and marks exactly five damage")
    void dealsExactlyFiveDamageToFriendlyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinewoodsArmadillo());
        harness.setHand(player1, List.of(new ScorchingShot()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Spinewoods Armadillo");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Spinewoods Armadillo");
        harness.assertInGraveyard(player1, "Scorching Shot");
        harness.assertLife(player1, 20);
    }
}
