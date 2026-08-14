package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoulstoneSanctuaryTest extends BaseCardTest {

    @Test
    void tappingProducesColorlessMana() {
        Permanent sanctuary = addSanctuaryReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(sanctuary);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void resolvingAbilityMakesItA3x3VigilantCreatureWithAllCreatureTypes() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sanctuary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sanctuary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sanctuary, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sanctuary, Keyword.CHANGELING)).isTrue();
    }

    @Test
    void animatedSanctuaryIsStillALand() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, sanctuary)).isTrue();
    }

    @Test
    void animationResetsAtEndOfTurn() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sanctuary)).isFalse();
        assertThat(gqs.hasKeyword(gd, sanctuary, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sanctuary, Keyword.CHANGELING)).isFalse();
    }

    @Test
    void activatingAbilityConsumesFourManaWithoutTappingTheLand() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sanctuary.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    private Permanent addSanctuaryReady(Player player) {
        SoulstoneSanctuary card = new SoulstoneSanctuary();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
