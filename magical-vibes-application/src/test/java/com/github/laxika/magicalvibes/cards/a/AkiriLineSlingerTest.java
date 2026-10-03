package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkiriLineSlinger.class, GrizzlyBears.class, Ornithopter.class, Spellbook.class})
class AkiriLineSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Akiri gets +1/+0 for each artifact its controller controls")
    void getsPowerForEachArtifactYouControl() {
        Permanent akiri = harness.addToBattlefieldAndReturn(player1, new AkiriLineSlinger());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, akiri)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, akiri)).isEqualTo(3);
    }

    @Test
    @DisplayName("Akiri does not count non-artifacts or artifacts controlled by an opponent")
    void onlyCountsArtifactsYouControl() {
        Permanent akiri = harness.addToBattlefieldAndReturn(player1, new AkiriLineSlinger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, akiri)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, akiri)).isEqualTo(3);
    }

    @Test
    @DisplayName("Akiri's power updates when artifacts enter and leave the battlefield")
    void powerUpdatesWithArtifactCount() {
        Permanent akiri = harness.addToBattlefieldAndReturn(player1, new AkiriLineSlinger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, akiri)).isEqualTo(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, akiri)).isZero();
    }
}
