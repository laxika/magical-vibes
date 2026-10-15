package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathswornGiant.class, Watchwolf.class})
class OathswornGiantTest extends BaseCardTest {

    @Test
    @DisplayName("The Giant and creatures granted vigilance remain untapped when attacking")
    void vigilanceKeepsGiantAndOtherCreaturesUntappedWhenAttacking() {
        Permanent giant = addCreatureReady(player1, new OathswornGiant());
        Permanent watchwolf = addCreatureReady(player1, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(giant.isAttacking()).isTrue();
        assertThat(watchwolf.isAttacking()).isTrue();
        assertThat(giant.isTapped()).isFalse();
        assertThat(watchwolf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other creatures you control get +0/+2 and vigilance")
    void buffsOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new Watchwolf());
        Permanent watchwolf = findPermanent(player1, "Watchwolf");

        int basePower = gqs.getEffectivePower(gd, watchwolf);
        int baseToughness = gqs.getEffectiveToughness(gd, watchwolf);
        harness.addToBattlefield(player1, new OathswornGiant());

        assertThat(gqs.getEffectivePower(gd, watchwolf)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, watchwolf)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, watchwolf, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Oathsworn Giant does not buff itself")
    void doesNotBuffItself() {
        OathswornGiant giant = new OathswornGiant();
        giant.setPower(10);
        giant.setToughness(10);
        harness.addToBattlefield(player1, giant);

        Permanent permanent = findPermanent(player1, "Oathsworn Giant");

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not affect an opponent's creatures")
    void doesNotAffectOpponentsCreatures() {
        harness.addToBattlefield(player2, new Watchwolf());
        Permanent watchwolf = findPermanent(player2, "Watchwolf");

        int basePower = gqs.getEffectivePower(gd, watchwolf);
        int baseToughness = gqs.getEffectiveToughness(gd, watchwolf);
        harness.addToBattlefield(player1, new OathswornGiant());

        assertThat(gqs.getEffectivePower(gd, watchwolf)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, watchwolf)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, watchwolf, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The static bonus applies to creatures that enter after Oathsworn Giant")
    void buffsCreaturesEnteringAfterIt() {
        harness.addToBattlefield(player1, new OathswornGiant());
        harness.addToBattlefield(player1, new Watchwolf());

        Permanent watchwolf = findPermanent(player1, "Watchwolf");
        int buffedPower = gqs.getEffectivePower(gd, watchwolf);
        int buffedToughness = gqs.getEffectiveToughness(gd, watchwolf);

        assertThat(gqs.hasKeyword(gd, watchwolf, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Oathsworn Giant"));

        assertThat(gqs.getEffectivePower(gd, watchwolf)).isEqualTo(buffedPower);
        assertThat(gqs.getEffectiveToughness(gd, watchwolf)).isEqualTo(buffedToughness - 2);
        assertThat(gqs.hasKeyword(gd, watchwolf, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Two Oathsworn Giants buff each other but not themselves")
    void twoGiantsBuffEachOther() {
        OathswornGiant first = new OathswornGiant();
        first.setPower(10);
        first.setToughness(10);
        OathswornGiant second = new OathswornGiant();
        second.setPower(10);
        second.setToughness(10);
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);

        for (Permanent giant : findPermanents(player1, "Oathsworn Giant")) {
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(10);
            assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(12);
        }
    }
}
