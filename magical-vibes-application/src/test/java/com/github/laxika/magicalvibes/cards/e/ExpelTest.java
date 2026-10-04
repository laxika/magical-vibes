package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LoreholdCampus;
import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Expel.class, SpinedKarok.class, LoreholdCampus.class})
class ExpelTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target tapped creature")
    void exilesTargetTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        tappedCreature.tap();

        castExpel(tappedCreature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spined Karok"));
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        addTappedValidTarget(player1);
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());

        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature")
    void cannotTargetTappedNoncreature() {
        addTappedValidTarget(player1);
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new LoreholdCampus());
        tappedLand.tap();

        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tappedLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntappedBeforeResolution() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        tappedCreature.tap();

        castExpel(tappedCreature);
        tappedCreature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spined Karok");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can exile a tapped creature controlled by the caster")
    void exilesOwnTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        target.tap();

        castExpel(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotInGraveyard(player1, "Spined Karok");
    }

    @Test
    @DisplayName("Does not resolve if its target has left the battlefield")
    void fizzlesIfTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        target.tap();
        castExpel(target);

        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getId().equals(target.getCard().getId()))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Expel");
    }

    private void castExpel(Permanent target) {
        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
    }

    private void addTappedValidTarget(com.github.laxika.magicalvibes.model.Player player) {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player, new SpinedKarok());
        tappedCreature.tap();
    }
}
