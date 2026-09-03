package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.m.MtendaGriffin;
import com.github.laxika.magicalvibes.cards.u.UnyaroBeeSting;
import com.github.laxika.magicalvibes.cards.w.WindreaperFalcon;
import com.github.laxika.magicalvibes.cards.z.ZirilanOfTheClaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazeriderDrake.class, Incinerate.class, IronTuskElephant.class, MtendaGriffin.class,
        UnyaroBeeSting.class, WindreaperFalcon.class, ZirilanOfTheClaw.class})
class HazeriderDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Hazerider Drake")
    void redCreatureCannotBlock() {
        addCreatureReady(player1, new HazeriderDrake());
        addCreatureReady(player2, new WindreaperFalcon());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-red flyer can block Hazerider Drake")
    void nonRedFlyerCanBlock() {
        addCreatureReady(player1, new HazeriderDrake());
        Permanent blocker = addCreatureReady(player2, new MtendaGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A non-flying creature cannot block Hazerider Drake")
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new HazeriderDrake());
        addCreatureReady(player2, new IronTuskElephant());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Takes no combat damage from a red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new ZirilanOfTheClaw());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new HazeriderDrake());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hazerider Drake");
    }

    @Test
    @DisplayName("Cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent drake = addCreatureReady(player2, new HazeriderDrake());
        addCreatureReady(player2, new IronTuskElephant());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, drake.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by a green spell")
    void canBeTargetedByGreenSpell() {
        Permanent drake = addCreatureReady(player1, new HazeriderDrake());

        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0, drake.getId());

        assertThat(drake.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hazerider Drake");
    }
}
