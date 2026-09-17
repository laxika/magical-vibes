package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreneticRaptor.class, FugitiveWizard.class})
class FreneticRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Beasts can't block while Frenetic Raptor is on the battlefield")
    void beastsCannotBlock() {
        addCreatureReady(player1, new FreneticRaptor());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreneticRaptor());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beasts can't block");
    }

    @Test
    @DisplayName("A non-Beast creature can still block")
    void nonBeastCanBlock() {
        addCreatureReady(player1, new FreneticRaptor());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A non-Beast creature can block a Beast")
    void nonBeastCanBlockBeast() {
        addCreatureReady(player1, new FreneticRaptor());
        Permanent attacker = addCreatureReady(player1, new FreneticRaptor());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
