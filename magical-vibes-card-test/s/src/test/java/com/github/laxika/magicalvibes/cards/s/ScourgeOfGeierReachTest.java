package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeOfGeierReach.class, WalkingCorpse.class, Mountain.class})
class ScourgeOfGeierReachTest extends BaseCardTest {

    @Test
    @DisplayName("Without opponent creatures, is 3/3")
    void withoutOpponentCreaturesIs3x3() {
        Permanent scourge = addScourge(player1);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    @Test
    @DisplayName("With one opponent creature, is 4/4")
    void withOneOpponentCreatureIs4x4() {
        Permanent scourge = addScourge(player1);
        addCreature(player2);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(4);
    }

    @Test
    @DisplayName("With three opponent creatures, is 6/6")
    void withThreeOpponentCreaturesIs6x6() {
        Permanent scourge = addScourge(player1);
        addCreature(player2);
        addCreature(player2);
        addCreature(player2);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(6);
    }

    @Test
    @DisplayName("Own creatures don't affect Scourge's power/toughness")
    void ownCreaturesDontCount() {
        Permanent scourge = addScourge(player1);
        addCreature(player1);
        addCreature(player1);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's non-creature permanents don't count")
    void opponentNonCreaturesDontCount() {
        Permanent scourge = addScourge(player1);
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus updates as opposing creatures enter and leave")
    void bonusUpdatesAsOpponentCreaturesEnterAndLeave() {
        Permanent scourge = addScourge(player1);
        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Scourge counts creatures controlled by its own opponents")
    void opposingScourgesUseTheirOwnControllers() {
        Permanent firstScourge = addScourge(player1);
        Permanent secondScourge = addScourge(player2);
        addCreature(player1);
        addCreature(player1);
        addCreature(player2);

        assertThat(gqs.getEffectivePower(gd, firstScourge)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, firstScourge)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondScourge)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, secondScourge)).isEqualTo(6);
    }

    private Permanent addScourge(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ScourgeOfGeierReach());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WalkingCorpse());
        perm.setSummoningSick(false);
    }
}
