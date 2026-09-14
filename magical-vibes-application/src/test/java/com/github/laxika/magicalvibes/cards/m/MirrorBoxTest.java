package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorBox.class, GrizzlyBears.class, TsaboTavoc.class})
class MirrorBoxTest extends BaseCardTest {

    @Test
    @DisplayName("Duplicate legendary permanents survive and legendary creatures get +1/+1")
    void ignoresLegendRuleAndBoostsLegendaryCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        int basePower = gqs.getEffectivePower(gd, first);
        int baseToughness = gqs.getEffectiveToughness(gd, first);

        Permanent box = harness.addToBattlefieldAndReturn(player1, new MirrorBox());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, box, second);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Only nontoken creatures get same-name bonuses, while tokens still count")
    void boostsNontokenCreaturesByOtherControlledCreaturesWithTheSameName() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, first);
        int baseToughness = gqs.getEffectiveToughness(gd, first);
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);

        harness.addToBattlefield(player1, new MirrorBox());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        int tokenPower = gqs.getEffectivePower(gd, token);
        int tokenToughness = gqs.getEffectiveToughness(gd, token);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(tokenPower);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(tokenToughness);
    }

    @Test
    @DisplayName("The legend-rule exemption does not protect permanents an opponent controls")
    void onlyExemptsPermanentsControlledByItsController() {
        harness.addToBattlefield(player1, new MirrorBox());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new TsaboTavoc());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TsaboTavoc());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }
}
