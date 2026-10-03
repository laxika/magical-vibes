package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondedConstruct.class, TimberpackWolf.class})
class BondedConstructTest extends BaseCardTest {

    @Test
    @DisplayName("Bonded Construct can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new BondedConstruct());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bonded Construct can attack alongside another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BondedConstruct());
        addCreatureReady(player1, new TimberpackWolf());

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Bonded Construct may block alone — the restriction covers attacking only")
    void canBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new TimberpackWolf());
        attacker.setAttacking(true);
        Permanent construct = addCreatureReady(player2, new BondedConstruct());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(construct.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Another creature staying back does not let Bonded Construct attack alone")
    void cantAttackWhenOtherCreatureDoesNotAttack() {
        addCreatureReady(player1, new BondedConstruct());
        addCreatureReady(player1, new TimberpackWolf());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Two Bonded Constructs can attack together")
    void twoConstructsCanAttackTogether() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BondedConstruct());
        addCreatureReady(player1, new BondedConstruct());

        declareAttackers(List.of(0, 1));

        harness.assertLife(player2, 16);
    }
}
