package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.s.Stratadon;
import com.github.laxika.magicalvibes.cards.t.ThunderscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlametongueKavu.class, Stratadon.class, ThunderscapeFamiliar.class, ManaCylix.class, Terminate.class})
class FlametongueKavuTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to target creature")
    void etbDealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player2, new Stratadon());
        harness.setHand(player1, List.of(new FlametongueKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Stratadon");
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Stratadon").getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB damage can destroy a target creature")
    void etbDamageDestroysTargetCreature() {
        harness.addToBattlefield(player2, new ThunderscapeFamiliar());
        harness.setHand(player1, List.of(new FlametongueKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Thunderscape Familiar");
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Thunderscape Familiar");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new ManaCylix());
        harness.setHand(player1, List.of(new FlametongueKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Mana Cylix");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("ETB must target itself when it is the only creature")
    void etbMustTargetItselfWhenAlone() {
        harness.castFromHand(player1, new FlametongueKavu(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flametongue Kavu");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Flametongue Kavu"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flametongue Kavu");
        harness.assertNotOnBattlefield(player1, "Flametongue Kavu");
    }

    @Test
    @DisplayName("ETB can target another creature its controller controls")
    void etbCanTargetOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new Stratadon());
        harness.castFromHand(player1, new FlametongueKavu(), "{3}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Flametongue Kavu");
    }

    @Test
    @DisplayName("ETB still deals damage after Flametongue Kavu leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        var target = harness.addToBattlefieldAndReturn(player2, new Stratadon());
        harness.castFromHand(player1, new FlametongueKavu(), "{3}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Flametongue Kavu"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Flametongue Kavu");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB does not retarget when its target leaves the battlefield")
    void etbDoesNotRetargetWhenTargetLeaves() {
        var target = harness.addToBattlefieldAndReturn(player2, new Stratadon());
        harness.castFromHand(player1, new FlametongueKavu(), "{3}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Stratadon");
        harness.assertOnBattlefield(player1, "Flametongue Kavu");
        assertThat(findPermanent(player1, "Flametongue Kavu").getMarkedDamage()).isZero();
    }
}
