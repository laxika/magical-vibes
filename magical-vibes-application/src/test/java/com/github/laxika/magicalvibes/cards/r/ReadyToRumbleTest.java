package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReadyToRumble.class, AirElemental.class, ChandraNalaar.class, FountainOfYouth.class})
class ReadyToRumbleTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(0, creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void dealsFiveDamageToTargetPlaneswalker() {
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        cast(0, planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(1, artifact);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    void damageModeCannotTargetAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactModeCannotTargetACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int modeIndex, Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, modeIndex, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ReadyToRumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
