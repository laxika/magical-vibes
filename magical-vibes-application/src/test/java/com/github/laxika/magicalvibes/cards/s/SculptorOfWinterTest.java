package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SculptorOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target snow land")
    void untapsTargetSnowLand() {
        addReadySculptor(player1);
        Permanent land = addSnowLand(player1);
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Sculptor of Winter").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's snow land")
    void untapsOpponentSnowLand() {
        addReadySculptor(player1);
        Permanent land = addSnowLand(player2);
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addReadySculptor(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addReadySculptor(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySculptor(Player player) {
        Permanent sculptor = new Permanent(new SculptorOfWinter());
        sculptor.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sculptor);
        return sculptor;
    }

    private Permanent addSnowLand(Player player) {
        Permanent land = new Permanent(new Forest());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
