package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoulderbornDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonSniper.class, BoulderbornDragon.class})
class DragonSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Dragon Sniper block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent sniper = addCreatureReady(player2, new DragonSniper());
        Permanent flyer = addAttackingFlyer();

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Vigilance keeps Dragon Sniper untapped after attacking")
    void vigilanceKeepsSniperUntappedAfterAttacking() {
        Permanent sniper = addCreatureReady(player1, new DragonSniper());

        declareAttackers(player1, List.of(0));

        assertThat(sniper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Deathtouch kills a larger creature Dragon Sniper damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent sniper = addCreatureReady(player2, new DragonSniper());
        Permanent flyer = addAttackingFlyer();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(flyer.getId()));
    }

    @Test
    @DisplayName("Reach does not let a tapped Dragon Sniper block")
    void tappedSniperCannotBlockFlyer() {
        Permanent sniper = addCreatureReady(player2, new DragonSniper());
        sniper.tap();
        Permanent flyer = addAttackingFlyer();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer)))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingFlyer() {
        Permanent flyer = addCreatureReady(player1, new BoulderbornDragon());
        flyer.setAttacking(true);
        return flyer;
    }

}
