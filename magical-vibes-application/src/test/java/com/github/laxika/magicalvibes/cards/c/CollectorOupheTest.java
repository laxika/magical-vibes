package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectorOuphe.class, SolRing.class, Forest.class})
class CollectorOupheTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks activated abilities of artifacts")
    void blocksArtifactAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        harness.addToBattlefield(player2, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Collector Ouphe");
    }

    @Test
    @DisplayName("Does not block activated abilities of non-artifact permanents")
    void doesNotBlockNonArtifactAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Collector Ouphe re-enables artifact abilities")
    void removingCollectorOupheReenablesArtifactAbilities() {
        Permanent ouphe = harness.addToBattlefieldAndReturn(player1, new CollectorOuphe());
        harness.addToBattlefield(player2, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(ouphe);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
