package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.cards.t.TajuruParagon;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalakirBloodPriest.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, InspiringCleric.class, TajuruParagon.class, VanquishTheWeak.class})
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

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    void countsItselfAsTheOnlyPartyMember() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        castBloodPriest();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    void duplicateClericsDoNotIncreasePartySize() {
        harness.addToBattlefield(player1, new MalakirBloodPriest());
        harness.addToBattlefield(player1, new MalakirBloodPriest());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        castBloodPriest();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    void creatureWithAllPartyTypesFillsOnlyOneAvailableRole() {
        harness.addToBattlefield(player1, new TajuruParagon());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        castBloodPriest();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    void removingAnotherPartyMemberBeforeResolutionReducesTheDrain() {
        UUID paragonId = harness.addToBattlefieldAndReturn(player1, new TajuruParagon()).getId();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.castFromHand(player1, new MalakirBloodPriest(), "{1}{B}");
        harness.passBothPriorities();

        destroyBeforeTriggerResolves(paragonId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    void triggerStillResolvesAfterPriestLeavesUsingTheRemainingParty() {
        harness.addToBattlefield(player1, new TajuruParagon());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.castFromHand(player1, new MalakirBloodPriest(), "{1}{B}");
        harness.passBothPriorities();

        destroyBeforeTriggerResolves(harness.getPermanentId(player1, "Malakir Blood-Priest"));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    void emptyPartyAtResolutionDoesNotChangeLifeTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.castFromHand(player1, new MalakirBloodPriest(), "{1}{B}");
        harness.passBothPriorities();

        destroyBeforeTriggerResolves(harness.getPermanentId(player1, "Malakir Blood-Priest"));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    private void destroyBeforeTriggerResolves(UUID permanentId) {
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    private void addFullParty(Player player) {
        harness.addToBattlefield(player, new InspiringCleric());
        harness.addToBattlefield(player, new FaerieMiscreant());
        harness.addToBattlefield(player, new BoggartBrute());
        harness.addToBattlefield(player, new FugitiveWizard());
    }

    private void castBloodPriest() {
        harness.castFromHand(player1, new MalakirBloodPriest(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
