package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunQuanLordOfWu.class, ShuFootSoldiers.class})
class SunQuanLordOfWuTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain horsemanship")
    void ownCreaturesGainHorsemanship() {
        Permanent creatures = addCreatureReady(player1, new ShuFootSoldiers());
        addCreatureReady(player1, new SunQuanLordOfWu());

        assertThat(gqs.hasKeyword(gd, creatures, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Sun Quan himself gains horsemanship")
    void sunQuanGainsHorsemanship() {
        Permanent sunQuan = addCreatureReady(player1, new SunQuanLordOfWu());

        assertThat(gqs.hasKeyword(gd, sunQuan, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain horsemanship")
    void opponentCreaturesDoNotGainHorsemanship() {
        Permanent opponentCreature = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player1, new SunQuanLordOfWu());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    @DisplayName("A creature without horsemanship cannot block a creature with horsemanship")
    void creatureWithoutHorsemanshipCannotBlock() {
        addCreatureReady(player1, new SunQuanLordOfWu());
        addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(horsemanship)");
    }

    @Test
    @DisplayName("A creature with horsemanship can block a creature with horsemanship")
    void creatureWithHorsemanshipCanBlock() {
        addCreatureReady(player1, new SunQuanLordOfWu());
        Permanent blocker = addCreatureReady(player2, new SunQuanLordOfWu());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Horsemanship is removed when Sun Quan leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent creatures = addCreatureReady(player1, new ShuFootSoldiers());
        Permanent sunQuan = addCreatureReady(player1, new SunQuanLordOfWu());
        assertThat(gqs.hasKeyword(gd, creatures, Keyword.HORSEMANSHIP)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .remove(sunQuan);

        assertThat(gqs.hasKeyword(gd, creatures, Keyword.HORSEMANSHIP)).isFalse();
    }
}
