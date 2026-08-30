package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CastleArdenvale.class, Plains.class})
class CastleArdenvaleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a Plains")
    void entersTappedWithoutPlains() {
        harness.setHand(player1, List.of(new CastleArdenvale()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Ardenvale").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new CastleArdenvale()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Ardenvale").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana ability adds white mana")
    void manaAbilityAddsWhite() {
        harness.addToBattlefield(player1, new CastleArdenvale());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Castle Ardenvale");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability creates a Human token")
    void tokenAbilityCreatesHumanToken() {
        harness.addToBattlefield(player1, new CastleArdenvale());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> "Human".equals(permanent.getCard().getName()));
    }
}
