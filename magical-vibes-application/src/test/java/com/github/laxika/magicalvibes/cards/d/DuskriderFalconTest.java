package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.c.CloudDjinn;
import com.github.laxika.magicalvibes.cards.c.CoilsOfTheMedusa;
import com.github.laxika.magicalvibes.cards.f.FledglingDjinn;
import com.github.laxika.magicalvibes.cards.s.SpinningDarkness;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
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

@CardUsed({
        BenalishKnight.class,
        CloudDjinn.class,
        CoilsOfTheMedusa.class,
        DuskriderFalcon.class,
        FledglingDjinn.class,
        SpinningDarkness.class,
        Thunderbolt.class
})
class DuskriderFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot block Duskrider Falcon")
    void blackCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new DuskriderFalcon());
        attacker.setAttacking(true);

        addCreatureReady(player2, new FledglingDjinn());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Blue creature can block Duskrider Falcon")
    void blueCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new DuskriderFalcon());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CloudDjinn());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Duskrider Falcon takes no combat damage from a black creature")
    void takesNoDamageFromBlack() {
        Permanent attacker = addCreatureReady(player1, new FledglingDjinn());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new DuskriderFalcon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Duskrider Falcon");
    }

    @Test
    @DisplayName("Cannot be targeted by a black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent falcon = addCreatureReady(player2, new DuskriderFalcon());

        addCreatureReady(player2, new BenalishKnight());

        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent falcon = addCreatureReady(player1, new DuskriderFalcon());

        harness.setHand(player1, List.of(new Thunderbolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 1, falcon.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Thunderbolt");
    }

    @Test
    @DisplayName("Black Aura cannot enchant Duskrider Falcon")
    void cannotBeEnchantedByBlackAura() {
        Permanent falcon = addCreatureReady(player2, new DuskriderFalcon());

        harness.setHand(player1, List.of(new CoilsOfTheMedusa()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
