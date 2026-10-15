package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CanopyCrawler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreneticRaptor.class, FugitiveWizard.class, CanopyCrawler.class})
class FreneticRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Frenetic Raptor prevents itself from blocking without another Raptor")
    void cannotBlockItself() {
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
    @DisplayName("Frenetic Raptor prevents other Beasts from blocking")
    void otherBeastCannotBlock() {
        addCreatureReady(player1, new FreneticRaptor());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CanopyCrawler());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beasts can't block");
    }

    @Test
    @DisplayName("Beasts can block again after the only Frenetic Raptor leaves the battlefield")
    void restrictionEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new FreneticRaptor());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CanopyCrawler());

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beasts can't block");

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

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
