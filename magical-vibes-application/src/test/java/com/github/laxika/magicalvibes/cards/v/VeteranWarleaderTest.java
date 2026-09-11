package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeteranWarleader.class, HadaFreeblade.class, GrizzlyBears.class})
class VeteranWarleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void powerAndToughnessCountControlledCreatures() {
        Permanent warleader = addWarleaderReady(player1);
        Permanent ownAlly = addCreatureReady(player1, new HadaFreeblade());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, warleader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warleader)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(ownAlly);

        assertThat(gqs.getEffectivePower(gd, warleader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warleader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping another Ally grants a chosen keyword until end of turn")
    void tapsAnotherAllyAndGrantsChosenKeyword() {
        Permanent warleader = addWarleaderReady(player1);
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(warleader), 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TRAMPLE");

        assertThat(ally.isTapped()).isTrue();
        assertThat(warleader.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, warleader, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warleader, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability offers first strike, vigilance, and trample")
    void canChooseEachKeyword() {
        Permanent warleader = addWarleaderReady(player1);
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        chooseKeyword(warleader, ally, "FIRST_STRIKE");
        chooseKeyword(warleader, ally, "VIGILANCE");
        chooseKeyword(warleader, ally, "TRAMPLE");

        assertThat(gqs.hasKeyword(gd, warleader, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warleader, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warleader, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ability requires another untapped Ally")
    void requiresAnotherUntappedAlly() {
        Permanent warleader = addWarleaderReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(warleader), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");

        Permanent ally = addCreatureReady(player1, new HadaFreeblade());
        ally.tap();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(warleader), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    private Permanent addWarleaderReady(Player player) {
        return addCreatureReady(player, new VeteranWarleader());
    }

    private void chooseKeyword(Permanent warleader, Permanent ally, String keyword) {
        ally.untap();
        harness.activateAbility(player1, battlefieldIndex(warleader), 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, keyword);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
