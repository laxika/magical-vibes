package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AphettoGrifter.class, Forest.class, GrizzlyBears.class})
class AphettoGrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two untapped Wizards to tap target permanent")
    void tapsTwoUntappedWizardsToTapTargetPermanent() {
        Permanent grifter = addReady(player1, new AphettoGrifter());
        Permanent wizard = addReady(player1, new AphettoGrifter());
        Permanent nonWizard = addReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(grifter), null, target.getId());

        assertThat(grifter.isTapped()).isTrue();
        assertThat(wizard.isTapped()).isTrue();
        assertThat(nonWizard.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two untapped Wizards")
    void cannotActivateWithoutTwoUntappedWizards() {
        Permanent grifter = addReady(player1, new AphettoGrifter());
        Permanent nonWizard = addReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(grifter),
                null,
                nonWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
