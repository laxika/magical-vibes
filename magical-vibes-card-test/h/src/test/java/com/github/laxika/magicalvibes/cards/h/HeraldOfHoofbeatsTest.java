package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HeraldOfHoofbeats.class, VenerableKnight.class, GrizzlyBears.class})
class HeraldOfHoofbeatsTest extends BaseCardTest {

    @Test
    @DisplayName("Herald of Hoofbeats gives other Knights you control horsemanship")
    void givesOtherKnightsHorsemanship() {
        harness.addToBattlefield(player1, new HeraldOfHoofbeats());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Herald of Hoofbeats does not give horsemanship to non-Knights or opposing Knights")
    void onlyGivesHorsemanshipToOtherKnightsYouControl() {
        harness.addToBattlefield(player1, new HeraldOfHoofbeats());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingKnight = harness.addToBattlefieldAndReturn(player2, new VenerableKnight());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HORSEMANSHIP)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    void heraldCannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent herald = addCreatureReady(player1, new HeraldOfHoofbeats());
        herald.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    void grantedHorsemanshipPreventsOrdinaryBlockers() {
        harness.addToBattlefield(player1, new HeraldOfHoofbeats());
        Permanent knight = addCreatureReady(player1, new VenerableKnight());
        knight.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    void knightWithGrantedHorsemanshipCanBlockHerald() {
        Permanent attacker = addCreatureReady(player1, new HeraldOfHoofbeats());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new HeraldOfHoofbeats());
        addCreatureReady(player2, new VenerableKnight());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void horsemanshipDoesNotRestrictWhatHeraldCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HeraldOfHoofbeats());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void knightsLoseGrantedHorsemanshipWhenHeraldLeavesBattlefield() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfHoofbeats());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.HORSEMANSHIP)).isTrue();

        herald.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Herald of Hoofbeats");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.HORSEMANSHIP)).isFalse();
    }
}
