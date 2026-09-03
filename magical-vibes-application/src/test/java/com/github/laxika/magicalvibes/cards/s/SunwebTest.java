package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KarooMeerkat;
import com.github.laxika.magicalvibes.cards.p.PearlDragon;
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

@CardUsed({Sunweb.class, PearlDragon.class, KarooMeerkat.class})
class SunwebTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Sunweb onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Sunweb()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sunweb");
    }

    @Test
    @DisplayName("Sunweb can block a flying creature with power 3 or greater")
    void canBlockHighPowerCreature() {
        Permanent sunweb = addCreatureReady(player2, new Sunweb());

        Permanent atkPerm = addCreatureReady(player1, new PearlDragon()); // 4/4 flying
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sunweb.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sunweb cannot block a creature with power 2 or less")
    void cannotBlockLowPowerCreature() {
        addCreatureReady(player2, new Sunweb());

        Permanent atkPerm = addCreatureReady(player1, new KarooMeerkat()); // 2/1
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 3 or greater");
    }

    @Test
    @DisplayName("Sunweb cannot attack because it has defender")
    void cannotAttackBecauseOfDefender() {
        addCreatureReady(player1, new Sunweb());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
