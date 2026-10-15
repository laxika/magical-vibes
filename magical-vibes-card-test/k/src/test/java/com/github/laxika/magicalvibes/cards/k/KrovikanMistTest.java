package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.p.PhobianPhantasm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    @DisplayName("Illusion cards outside the battlefield do not increase Krovikan Mist's size")
    void ignoresIllusionsOutsideBattlefield() {
        Permanent mist = addCreatureReady(player1, new KrovikanMist());
        harness.setHand(player1, List.of(new PhobianPhantasm()));
        harness.setGraveyard(player2, List.of(new PhobianPhantasm()));
        harness.setExile(player1, List.of(new PhobianPhantasm()));
        harness.setLibrary(player2, List.of(new PhobianPhantasm()));

        assertThat(gqs.getEffectivePower(gd, mist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Krovikan Mist defines its size in hand and graveyard without counting itself there")
    void definesSizeOutsideBattlefield() {
        KrovikanMist inHand = new KrovikanMist();
        KrovikanMist inGraveyard = new KrovikanMist();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player2, List.of(inGraveyard));

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new PhobianPhantasm());
        harness.addToBattlefield(player2, new KrovikanMist());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Krovikan Mists count one another without recursive size evaluation")
    void multipleMistsCountEachOther() {
        Permanent first = addCreatureReady(player1, new KrovikanMist());
        Permanent second = addCreatureReady(player2, new KrovikanMist());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}
