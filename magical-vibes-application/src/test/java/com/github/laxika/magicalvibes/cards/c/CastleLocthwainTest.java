package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CastleLocthwain.class, Swamp.class})
class CastleLocthwainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a Swamp")
    void entersTappedWithoutSwamp() {
        harness.setHand(player1, List.of(new CastleLocthwain()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Locthwain").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new CastleLocthwain()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Locthwain").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The first ability adds one black mana")
    void firstAbilityAddsBlackMana() {
        harness.addToBattlefield(player1, new CastleLocthwain());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Castle Locthwain");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability draws before losing life equal to the resulting hand size")
    void drawsBeforeLosingLifeEqualToResultingHandSize() {
        harness.addToBattlefield(player1, new CastleLocthwain());
        harness.setHand(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }
}
