package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({DragonSniper.class, AirElemental.class})
class DragonSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Dragon Sniper block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent sniper = addCreatureReady(player2, new DragonSniper());
        Permanent flyer = addAttackingFlyer();

        prepareBlockerDeclaration();

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

        prepareBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(flyer.getId()));
    }

    private Permanent addAttackingFlyer() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        flyer.setAttacking(true);
        return flyer;
    }

    private void prepareBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
