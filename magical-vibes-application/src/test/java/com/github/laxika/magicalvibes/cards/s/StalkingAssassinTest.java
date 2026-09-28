package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientSpring;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalkingAssassin.class, RagingKavu.class, AncientSpring.class})
class StalkingAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature")
    void tapsTargetCreature() {
        Permanent assassin = addReadyAssassin(player1);
        Permanent target = addUntappedKavu(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(assassin.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot tap a noncreature permanent")
    void cannotTapNoncreaturePermanent() {
        addReadyAssassin(player1);
        Permanent target = addTappedSpring(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Destroys target tapped creature")
    void destroysTargetTappedCreature() {
        addReadyAssassin(player1);
        Permanent target = addTappedKavu(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        harness.assertInGraveyard(player2, "Raging Kavu");
    }

    @Test
    @DisplayName("Cannot destroy an untapped creature")
    void cannotDestroyUntappedCreature() {
        addReadyAssassin(player1);
        Permanent target = addUntappedKavu(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot destroy a tapped noncreature permanent")
    void cannotDestroyTappedNoncreaturePermanent() {
        addReadyAssassin(player1);
        Permanent target = addTappedSpring(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Destruction fizzles if the target becomes untapped before resolution")
    void destructionFizzlesIfTargetBecomesUntappedBeforeResolution() {
        addReadyAssassin(player1);
        Permanent target = addTappedKavu(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Raging Kavu");
    }

    private Permanent addReadyAssassin(Player player) {
        return addCreatureReady(player, new StalkingAssassin());
    }

    private Permanent addTappedKavu(Player player) {
        Permanent perm = addCreatureReady(player, new RagingKavu());
        perm.tap();
        return perm;
    }

    private Permanent addUntappedKavu(Player player) {
        return addCreatureReady(player, new RagingKavu());
    }

    private Permanent addTappedSpring(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AncientSpring());
        perm.tap();
        return perm;
    }
}
