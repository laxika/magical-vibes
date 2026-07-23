package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HallowedGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns target nonsnow land you control to its owner's hand")
    void bouncesControlledNonsnowLand() {
        addHallowedGround(player1);
        Permanent plains = addLand(player1, false);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot target a snow land")
    void cannotTargetSnowLand() {
        addHallowedGround(player1);
        Permanent snowLand = addLand(player1, true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetOpponentsLand() {
        addHallowedGround(player1);
        Permanent plains = addLand(player2, false);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addHallowedGround(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHallowedGround(Player player) {
        Permanent perm = new Permanent(new HallowedGround());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player player, boolean snow) {
        Permanent land = new Permanent(new Plains());
        if (snow) {
            TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        }
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
