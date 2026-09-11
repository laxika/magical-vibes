package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vengeance.class, ShuFootSoldiers.class, Forest.class})
class VengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target tapped creature")
    void resolvingDestroysTargetTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new Vengeance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0, tappedCreature.getId());

        harness.assertNotOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertInGraveyard(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Can target a tapped creature you control")
    void canTargetTappedCreatureYouControl() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new Vengeance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0, tappedCreature.getId());

        harness.assertNotOnBattlefield(player1, "Shu Foot Soldiers");
        harness.assertInGraveyard(player1, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent tappedValid = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        tappedValid.tap();

        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new Vengeance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature")
    void cannotTargetTappedNonCreature() {
        Permanent tappedValid = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        tappedValid.tap();

        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedLand.tap();

        harness.setHand(player1, List.of(new Vengeance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, tappedLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if target creature becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new Vengeance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, tappedCreature.getId());

        tappedCreature.untap();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
