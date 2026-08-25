package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HuntedDragon.class)
class HuntedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates three first-strike Knight tokens under the targeted opponent's control")
    void etbCreatesKnightTokensForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> knights = findPermanents(player2, "Knight");
        assertThat(knights).hasSize(3);
        assertThat(findPermanents(player1, "Knight")).isEmpty();
        assertThat(knights).allMatch(knight -> knight.hasKeyword(Keyword.FIRST_STRIKE));
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
