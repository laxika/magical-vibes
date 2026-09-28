package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DisruptingShoal;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.s.SilverstormSamurai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IsaoEnlightenedBushi.class, DisruptingShoal.class, GnarledMass.class,
        SilverstormSamurai.class, HighGround.class})
class IsaoEnlightenedBushiTest extends BaseCardTest {

    @Test
    void cannotBeCountered() {
        IsaoEnlightenedBushi isao = new IsaoEnlightenedBushi();
        harness.setHand(player1, List.of(isao));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new DisruptingShoal()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, isao.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(isao.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(isao.getId()));
    }

    @Test
    void bushidoTriggersWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        attacker.setAttacking(true);
        Permanent isao = addCreatureReady(player2, new IsaoEnlightenedBushi());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, isao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isao)).isEqualTo(3);
    }

    @Test
    void bushidoTriggersOnlyOnceWhenBlockingMultipleCreatures() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent isao = addCreatureReady(player2, new IsaoEnlightenedBushi());
        Permanent firstAttacker = addCreatureReady(player1, new GnarledMass());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GnarledMass());
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, isao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isao)).isEqualTo(3);
    }

    @Test
    void bushidoTriggersWhenBecomesBlocked() {
        Permanent isao = addCreatureReady(player1, new IsaoEnlightenedBushi());
        isao.setAttacking(true);
        addCreatureReady(player2, new GnarledMass());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, isao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isao)).isEqualTo(3);
    }

    @Test
    void regeneratesTargetSamurai() {
        harness.addToBattlefield(player1, new IsaoEnlightenedBushi());
        Permanent samurai = addCreatureReady(player2, new SilverstormSamurai());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, samurai.getId());
        harness.passBothPriorities();

        assertThat(samurai.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotRegenerateNonSamurai() {
        harness.addToBattlefield(player1, new IsaoEnlightenedBushi());
        Permanent nonSamurai = addCreatureReady(player2, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonSamurai.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Samurai");
    }
}
