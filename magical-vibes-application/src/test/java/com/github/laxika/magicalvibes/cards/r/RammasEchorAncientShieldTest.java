package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RammasEchorAncientShield.class, LightningBolt.class, WallOfStone.class, GrizzlyBears.class})
class RammasEchorAncientShieldTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell each turn draws a card and creates a defender Wall")
    void secondSpellDrawsAndCreatesWall() {
        harness.addToBattlefield(player1, new RammasEchorAncientShield());
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        List<Permanent> walls = findPermanents(player1, "Wall");
        assertThat(walls).hasSize(1);
        Permanent wall = walls.getFirst();
        assertThat(wall.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("At combat, only your defender creatures gain exalted until end of turn")
    void grantsExaltedToControlledDefendersAtCombat() {
        harness.addToBattlefield(player1, new RammasEchorAncientShield());
        Permanent ownWall = addCreatureReady(player1, new WallOfStone());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentWall = addCreatureReady(player2, new WallOfStone());

        advanceToBeginningOfCombat(player1);

        assertThat(gqs.hasKeyword(gd, ownWall, Keyword.EXALTED)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.EXALTED)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentWall, Keyword.EXALTED)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, ownWall, Keyword.EXALTED)).isFalse();
    }

    private void advanceToBeginningOfCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
