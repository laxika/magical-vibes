package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.r.RustGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyRiposte.class, ArgothianSprite.class, RustGoliath.class})
class DeadlyRiposteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a tapped creature and gains 2 life")
    void damagesTappedCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RustGoliath());
        target.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DeadlyRiposte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent validTarget = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        validTarget.tap();
        Permanent untappedTarget = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new DeadlyRiposte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        target.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DeadlyRiposte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Lethal damage still gains life for the spell's controller")
    void lethalDamageStillGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        target.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DeadlyRiposte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        harness.assertInGraveyard(player2, "Argothian Sprite");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target your own tapped creature")
    void canTargetOwnTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RustGoliath());
        target.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DeadlyRiposte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not gain life if the target leaves the battlefield")
    void noLifeGainWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        target.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DeadlyRiposte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Deadly Riposte");
    }
}
