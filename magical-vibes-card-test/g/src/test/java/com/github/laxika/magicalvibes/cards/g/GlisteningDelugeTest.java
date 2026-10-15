package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArchivist;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlisteningDeluge.class, GrizzlyBears.class, YouthfulKnight.class, HillGiant.class,
        Forest.class, GhaltaAndMavren.class, PhyrexianArchivist.class})
class GlisteningDelugeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives green and white creatures an additional -2/-2")
    void givesAdditionalDebuffToGreenAndWhiteCreatures() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        Permanent red = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        castDeluge();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(green);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(white);
        assertThat(red.getEffectivePower()).isEqualTo(2);
        assertThat(red.getEffectiveToughness()).isEqualTo(2);
        assertThat(land.getEffectivePower()).isEqualTo(0);
        assertThat(land.getEffectiveToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuffs expire at end of turn")
    void debuffsExpireAtEndOfTurn() {
        Permanent red = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        castDeluge();
        assertThat(red.getEffectivePower()).isEqualTo(2);
        assertThat(red.getEffectiveToughness()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(red.getEffectivePower()).isEqualTo(3);
        assertThat(red.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A green and white creature gets the additional reduction only once")
    void greenAndWhiteCreatureGetsAdditionalReductionOnce() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GhaltaAndMavren());

        castDeluge();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getEffectivePower()).isEqualTo(9);
        assertThat(creature.getEffectiveToughness()).isEqualTo(9);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getEffectivePower()).isEqualTo(12);
        assertThat(creature.getEffectiveToughness()).isEqualTo(12);
    }

    @Test
    @DisplayName("Colorless artifact creatures get only -1/-1")
    void colorlessArtifactCreatureGetsOnlyBaseReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PhyrexianArchivist());

        castDeluge();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void creaturesEnteringAfterResolutionAreUnaffected() {
        castDeluge();

        Permanent multicolor = harness.addToBattlefieldAndReturn(player2, new GhaltaAndMavren());
        Permanent colorless = harness.addToBattlefieldAndReturn(player1, new PhyrexianArchivist());

        assertThat(multicolor.getEffectivePower()).isEqualTo(12);
        assertThat(multicolor.getEffectiveToughness()).isEqualTo(12);
        assertThat(colorless.getEffectivePower()).isEqualTo(4);
        assertThat(colorless.getEffectiveToughness()).isEqualTo(5);
    }

    private void castDeluge() {
        harness.setHand(player1, List.of(new GlisteningDeluge()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
