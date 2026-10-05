package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GoblinRoughrider;
import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrenkosEnforcer.class, OreskosSwiftclaw.class, BlackCat.class,
        GoblinRoughrider.class, BronzeSable.class})
class KrenkosEnforcerTest extends BaseCardTest {

    @Test
    void whiteNonartifactCreatureCannotBlock() {
        Permanent enforcer = addCreatureReady(player1, new KrenkosEnforcer());
        addCreatureReady(player2, new OreskosSwiftclaw());
        enforcer.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void blackNonartifactCreatureCannotBlock() {
        Permanent enforcer = addCreatureReady(player1, new KrenkosEnforcer());
        addCreatureReady(player2, new BlackCat());
        enforcer.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void redNonartifactCreatureCanBlock() {
        Permanent enforcer = addCreatureReady(player1, new KrenkosEnforcer());
        Permanent blocker = addCreatureReady(player2, new GoblinRoughrider());
        enforcer.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void colorlessArtifactCreatureCanBlock() {
        Permanent enforcer = addCreatureReady(player1, new KrenkosEnforcer());
        Permanent blocker = addCreatureReady(player2, new BronzeSable());
        enforcer.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateDoesNotRestrictEnforcersOwnBlocking() {
        Permanent attacker = addCreatureReady(player1, new OreskosSwiftclaw());
        Permanent enforcer = addCreatureReady(player2, new KrenkosEnforcer());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(enforcer.isBlocking()).isTrue();
    }
}
