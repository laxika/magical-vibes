package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsClinic.class, GrizzlyBears.class})
class WitchsClinicTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new WitchsClinic());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives a commander lifelink until end of turn")
    void givesCommanderLifelinkUntilEndOfTurn() {
        Permanent clinic = harness.addToBattlefieldAndReturn(player1, new WitchsClinic());
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        commander.setCommander(true);
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, commander.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, commander, Keyword.LIFELINK)).isTrue();
        assertThat(clinic.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, commander, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-commander")
    void cannotTargetNonCommander() {
        harness.addToBattlefield(player1, new WitchsClinic());
        Permanent nonCommander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonCommander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
