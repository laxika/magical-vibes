package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fluttershy.class, GrizzlyBears.class})
class FluttershyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on each creature the target player controls and stares down the optional target")
    void resolvesBothTargetGroups() {
        Permanent fluttershy = addCreatureReady(player1, new Fluttershy());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        activate(fluttershy, List.of(player2.getId(), targetCreature.getId()));

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(3);
        assertThat(targetCreature.isCantAttackThisTurn()).isTrue();
        assertThat(targetCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The creature target may be omitted")
    void mayOmitCreatureTarget() {
        Permanent fluttershy = addCreatureReady(player1, new Fluttershy());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        activate(fluttershy, List.of(player2.getId()));

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(3);
        assertThat(targetCreature.isCantAttackThisTurn()).isFalse();
        assertThat(targetCreature.isCantBlockThisTurn()).isFalse();
    }

    private void activate(Permanent fluttershy, List<java.util.UUID> targets) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(fluttershy),
                0,
                targets);
        harness.passBothPriorities();
    }
}
