package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodArtist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineBrushstroke.class, BloodArtist.class, GrizzlyBears.class})
class SanguineBrushstrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Blood token and conjures Blood Artist onto the battlefield")
    void entersWithBloodAndBloodArtist() {
        harness.setHand(player1, List.of(new SanguineBrushstroke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Blood Artist")).hasSize(1);
        assertThat(findPermanent(player1, "Blood Artist").getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Blood token makes each opponent lose 1 life and you gain 1 life")
    void bloodSacrificeDrainsOpponents() {
        harness.setHand(player1, List.of(new SanguineBrushstroke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = findPermanent(player1, "Blood");
        int bloodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blood);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, bloodIndex, null, null);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
