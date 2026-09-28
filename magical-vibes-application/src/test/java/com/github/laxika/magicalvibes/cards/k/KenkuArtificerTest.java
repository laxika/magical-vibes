package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KenkuArtificer.class, Millstone.class, Ornithopter.class, GrizzlyBears.class})
class KenkuArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts three counters on and permanently animates a noncreature artifact")
    void animatesTargetArtifact() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        castKenku(millstone.getId());

        millstone = findPermanent(player2, "Millstone");
        assertThat(millstone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, millstone, CardSubtype.HOMUNCULUS)).isTrue();
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can enter without choosing an optional target")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new KenkuArtificer()));
        addManaForKenku();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kenku Artificer");
    }

    @Test
    @DisplayName("Rejects an artifact creature as the target")
    void rejectsArtifactCreatureTarget() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new KenkuArtificer()));
        addManaForKenku();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact");
    }

    @Test
    @DisplayName("Rejects a non-artifact as the target")
    void rejectsNonArtifactTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KenkuArtificer()));
        addManaForKenku();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact");
    }

    private void castKenku(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new KenkuArtificer()));
        addManaForKenku();

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForKenku() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
