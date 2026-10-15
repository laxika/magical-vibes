package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stalwart Shield-Bearers")
@CardUsed({StalwartShieldBearers.class, OvergrownBattlement.class, GlorySeeker.class, Regress.class})
class StalwartShieldBearersTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other defending creatures you control")
    void boostsOtherDefendingCreaturesYouControl() {
        Permanent shieldBearers = harness.addToBattlefieldAndReturn(player1, new StalwartShieldBearers());
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        Permanent nonDefender = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opponentDefender = harness.addToBattlefieldAndReturn(player2, new OvergrownBattlement());

        assertThat(gqs.getEffectiveToughness(gd, shieldBearers)).isEqualTo(shieldBearers.getCard().getToughness());
        assertThat(gqs.getEffectiveToughness(gd, defender)).isEqualTo(defender.getCard().getToughness() + 2);
        assertThat(gqs.getEffectiveToughness(gd, nonDefender)).isEqualTo(nonDefender.getCard().getToughness());
        assertThat(gqs.getEffectiveToughness(gd, opponentDefender))
                .isEqualTo(opponentDefender.getCard().getToughness());
    }

    @Test
    @DisplayName("Multiple Shield-Bearers boost each other and stack on other defenders")
    void multipleShieldBearersStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StalwartShieldBearers());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StalwartShieldBearers());
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(first.getCard().getToughness() + 2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(second.getCard().getToughness() + 2);
        assertThat(gqs.getEffectiveToughness(gd, defender)).isEqualTo(defender.getCard().getToughness() + 4);
        assertThat(gqs.getEffectivePower(gd, defender)).isEqualTo(defender.getCard().getPower());
    }

    @Test
    @DisplayName("Defenders entering later gain the bonus and lose it when the source leaves")
    void bonusUpdatesWhenPermanentsEnterAndLeave() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StalwartShieldBearers());
        Permanent existingDefender = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        assertThat(gqs.getEffectiveToughness(gd, existingDefender))
                .isEqualTo(existingDefender.getCard().getToughness() + 2);

        Permanent newDefender = harness.enterBattlefieldAndReturn(player1, new OvergrownBattlement());
        assertThat(gqs.getEffectiveToughness(gd, newDefender))
                .isEqualTo(newDefender.getCard().getToughness() + 2);

        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertNotOnBattlefield(player1, "Stalwart Shield-Bearers");
        harness.assertInHand(player1, "Stalwart Shield-Bearers");
        assertThat(gqs.getEffectiveToughness(gd, existingDefender))
                .isEqualTo(existingDefender.getCard().getToughness());
        assertThat(gqs.getEffectiveToughness(gd, newDefender))
                .isEqualTo(newDefender.getCard().getToughness());
    }
}
