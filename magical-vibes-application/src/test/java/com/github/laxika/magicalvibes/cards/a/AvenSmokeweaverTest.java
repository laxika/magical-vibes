package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.m.MadDog;
import com.github.laxika.magicalvibes.cards.m.MuscleBurst;
import com.github.laxika.magicalvibes.cards.v.VampiricDragon;
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

@CardUsed({AvenSmokeweaver.class, Firebolt.class, MadDog.class, MuscleBurst.class, VampiricDragon.class})
class AvenSmokeweaverTest extends BaseCardTest {

    @Test
    @DisplayName("Aven Smokeweaver takes no combat damage from a red creature")
    void takesNoCombatDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player1, new MadDog());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AvenSmokeweaver());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Aven Smokeweaver");
    }

    @Test
    @DisplayName("A red creature cannot block Aven Smokeweaver")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new AvenSmokeweaver());
        attacker.setAttacking(true);
        addCreatureReady(player2, new VampiricDragon());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Aven Smokeweaver cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent smokeweaver = addCreatureReady(player2, new AvenSmokeweaver());

        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, smokeweaver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Aven Smokeweaver can be targeted by a green spell")
    void canBeTargetedByGreenSpell() {
        Permanent smokeweaver = addCreatureReady(player1, new AvenSmokeweaver());

        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, smokeweaver.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Muscle Burst");
    }
}
