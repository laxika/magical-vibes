package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.ButcherOrgg;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PinpointAvalanche.class, GlorySeeker.class, ButcherOrgg.class})
class PinpointAvalancheTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to and destroys target creature")
    void dealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new PinpointAvalanche()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Glory Seeker"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Damage cannot be prevented")
    void damageCannotBePrevented() {
        harness.addToBattlefield(player2, new GlorySeeker());
        Permanent target = findPermanent(player2, "Glory Seeker");
        target.setDamagePreventionShield(10);
        harness.setHand(player1, List.of(new PinpointAvalanche()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a creature you control")
    void dealsExactlyFourDamageToCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ButcherOrgg());
        harness.setHand(player1, List.of(new PinpointAvalanche()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Butcher Orgg");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new PinpointAvalanche()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
