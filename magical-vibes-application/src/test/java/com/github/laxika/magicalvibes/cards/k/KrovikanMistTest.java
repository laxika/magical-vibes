package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.p.PhobianPhantasm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrovikanMist.class, PhobianPhantasm.class, BorealDruid.class})
class KrovikanMistTest extends BaseCardTest {

    @Test
    @DisplayName("Krovikan Mist counts itself as an Illusion")
    void countsItself() {
        Permanent mist = addCreatureReady(player1, new KrovikanMist());

        assertThat(gqs.getEffectivePower(gd, mist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Krovikan Mist counts Illusions on all battlefields")
    void countsIllusionsOnAllBattlefields() {
        Permanent mist = addCreatureReady(player1, new KrovikanMist());
        harness.addToBattlefield(player1, new PhobianPhantasm());
        harness.addToBattlefield(player2, new PhobianPhantasm());

        assertThat(gqs.getEffectivePower(gd, mist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Krovikan Mist ignores non-Illusion permanents")
    void ignoresNonIllusions() {
        Permanent mist = addCreatureReady(player1, new KrovikanMist());
        harness.addToBattlefield(player1, new BorealDruid());

        assertThat(gqs.getEffectivePower(gd, mist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Krovikan Mist updates as Illusions enter and leave")
    void updatesWhenIllusionCountChanges() {
        Permanent mist = addCreatureReady(player1, new KrovikanMist());
        Permanent illusion = harness.addToBattlefieldAndReturn(player2, new PhobianPhantasm());

        assertThat(gqs.getEffectivePower(gd, mist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mist)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, illusion));

        assertThat(gqs.getEffectivePower(gd, mist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mist)).isEqualTo(1);
    }
}
