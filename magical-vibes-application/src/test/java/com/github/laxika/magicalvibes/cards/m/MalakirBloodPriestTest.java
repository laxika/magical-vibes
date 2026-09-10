package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalakirBloodPriest.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, InspiringCleric.class})
class MalakirBloodPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses and you gain life equal to your party size")
    void drainsForPartySize() {
        addFullParty(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        castBloodPriest();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts only party creatures controlled by the priest's controller")
    void countsOnlyControllersParty() {
        harness.addToBattlefield(player1, new FaerieMiscreant());
        addFullParty(player2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        castBloodPriest();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    private void addFullParty(Player player) {
        harness.addToBattlefield(player, new InspiringCleric());
        harness.addToBattlefield(player, new FaerieMiscreant());
        harness.addToBattlefield(player, new BoggartBrute());
        harness.addToBattlefield(player, new FugitiveWizard());
    }

    private void castBloodPriest() {
        harness.setHand(player1, List.of(new MalakirBloodPriest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
