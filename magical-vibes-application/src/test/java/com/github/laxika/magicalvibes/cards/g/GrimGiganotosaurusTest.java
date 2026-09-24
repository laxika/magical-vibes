package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.s.SerratedArrows;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimGiganotosaurus.class, CrawWurm.class, SerratedArrows.class})
class GrimGiganotosaurusTest extends BaseCardTest {

    @Test
    @DisplayName("High-power creatures opponents control reduce the monstrosity activation cost")
    void highPowerOpposingCreaturesReduceActivationCost() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        addCreatureReady(player2, new CrawWurm());
        addCreatureReady(player2, new CrawWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(dinosaur.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Becoming monstrous destroys all other artifacts and creatures")
    void becomingMonstrousDestroysOtherArtifactsAndCreatures() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        Permanent otherCreature = addCreatureReady(player1, new CrawWurm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SerratedArrows());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(dinosaur.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dinosaur)
                .doesNotContain(otherCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }
}
