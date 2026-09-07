package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CloakOfInvisibility;
import com.github.laxika.magicalvibes.cards.f.Foratog;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.s.SandbarCrocodile;
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

@CardUsed({KarooMeerkat.class, Boomerang.class, CloakOfInvisibility.class, Foratog.class,
        Incinerate.class, SandbarCrocodile.class})
class KarooMeerkatTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature cannot block Karoo Meerkat")
    void blueCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new KarooMeerkat());
        attacker.setAttacking(true);

        addCreatureReady(player2, new SandbarCrocodile());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Karoo Meerkat")
    void greenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new KarooMeerkat());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Foratog());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Karoo Meerkat takes no combat damage from a blue creature")
    void takesNoDamageFromBlue() {
        Permanent attacker = addCreatureReady(player1, new SandbarCrocodile());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new KarooMeerkat());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();

        harness.assertOnBattlefield(player2, "Karoo Meerkat");
    }

    @Test
    @DisplayName("Karoo Meerkat dies to combat damage from a green creature")
    void takesNormalDamageFromGreen() {
        Permanent attacker = addCreatureReady(player1, new Foratog());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new KarooMeerkat());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Karoo Meerkat");
    }

    @Test
    @DisplayName("Cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent meerkat = addCreatureReady(player2, new KarooMeerkat());
        addCreatureReady(player2, new Foratog());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, meerkat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent meerkat = addCreatureReady(player2, new KarooMeerkat());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, meerkat.getId());

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Karoo Meerkat");
    }

    @Test
    @DisplayName("Cannot be enchanted by a blue Aura")
    void cannotBeEnchantedByBlueAura() {
        Permanent meerkat = addCreatureReady(player2, new KarooMeerkat());

        harness.setHand(player1, List.of(new CloakOfInvisibility()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, meerkat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }
}
