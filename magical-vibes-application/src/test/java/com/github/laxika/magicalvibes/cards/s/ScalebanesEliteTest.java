package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkuDjinn;
import com.github.laxika.magicalvibes.cards.d.DarkPrivilege;
import com.github.laxika.magicalvibes.cards.h.HopeCharm;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.cards.w.WickedReward;
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

@CardUsed({ScalebanesElite.class, AkuDjinn.class, DarkPrivilege.class, HopeCharm.class,
        Warthog.class, WickedReward.class})
class ScalebanesEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot block Scalebane's Elite")
    void blackCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ScalebanesElite());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AkuDjinn());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Scalebane's Elite")
    void greenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new ScalebanesElite());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Warthog());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Takes no combat damage from black creature")
    void takesNoDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new AkuDjinn());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ScalebanesElite());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Scalebane's Elite");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent elite = addCreatureReady(player2, new ScalebanesElite());
        Permanent sacrifice = addCreatureReady(player1, new Warthog());

        harness.setHand(player1, List.of(new WickedReward()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, elite.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent elite = addCreatureReady(player1, new ScalebanesElite());

        harness.setHand(player1, List.of(new HopeCharm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, elite.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hope Charm");
    }

    @Test
    @DisplayName("Cannot be enchanted by black Aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent elite = addCreatureReady(player2, new ScalebanesElite());

        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, elite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
