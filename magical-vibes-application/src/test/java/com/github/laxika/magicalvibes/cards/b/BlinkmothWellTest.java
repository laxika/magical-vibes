package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.t.TelJiladChosen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlinkmothWell.class, Bonesplitter.class, IronMyr.class, TelJiladChosen.class})
class BlinkmothWellTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new BlinkmothWell());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays {2} and taps to tap a target noncreature artifact")
    void tapsTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new BlinkmothWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the tap ability pays {2} and taps Blinkmoth Well as a cost")
    void activationPaysManaAndTapsSource() {
        harness.addToBattlefield(player1, new BlinkmothWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player1, new BlinkmothWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new BlinkmothWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TelJiladChosen());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
