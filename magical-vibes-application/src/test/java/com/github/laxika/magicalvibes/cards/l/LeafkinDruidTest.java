package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafkinDruid.class, GreenwoodSentinel.class, Forest.class})
class LeafkinDruidTest extends BaseCardTest {

    @Test
    void addsOneGreenManaWithFewerThanFourCreatures() {
        addCreatureReady(player1, new LeafkinDruid());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void addsTwoGreenManaWithFourCreatures() {
        addCreatureReady(player1, new LeafkinDruid());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void resolvesImmediatelyAndTapsTheDruid() {
        Permanent druid = addCreatureReady(player1, new LeafkinDruid());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingCreaturesDoNotCount() {
        addCreatureReady(player1, new LeafkinDruid());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void noncreaturePermanentsDoNotCount() {
        addCreatureReady(player1, new LeafkinDruid());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tappedAndSummoningSickCreaturesStillCount() {
        addCreatureReady(player1, new LeafkinDruid());
        for (int i = 0; i < 3; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
            creature.setTapped(true);
            creature.setSummoningSick(true);
        }

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void moreThanFourCreaturesStillProducesOnlyTwoMana() {
        addCreatureReady(player1, new LeafkinDruid());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GreenwoodSentinel());
        }

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new LeafkinDruid());
        druid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(druid.isTapped()).isFalse();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new LeafkinDruid());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
