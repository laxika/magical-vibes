package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfaneTutor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EcstaticBeauty.class, ProfaneTutor.class, GrizzlyBears.class})
class EcstaticBeautyTest extends BaseCardTest {

    @Test
    void exilesTopThreeAndSuspendsOnlyExiledCardsWithSuspend() {
        ProfaneTutor alreadySuspended = suspendProfaneTutor();
        ProfaneTutor newlyExiledSuspended = new ProfaneTutor();
        GrizzlyBears nonSuspended = new GrizzlyBears();
        GrizzlyBears anotherNonSuspended = new GrizzlyBears();
        harness.setLibrary(player1, List.of(newlyExiledSuspended, nonSuspended, anotherNonSuspended));

        castBeauty();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(newlyExiledSuspended, nonSuspended, anotherNonSuspended);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(alreadySuspended.getId(), 2)
                .containsEntry(newlyExiledSuspended.getId(), 4)
                .doesNotContainKeys(nonSuspended.getId(), anotherNonSuspended.getId());
        for (Card card : List.of(newlyExiledSuspended, nonSuspended, anotherNonSuspended)) {
            assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
        }
    }

    @Test
    void suspendsFromHandWithFourTimeCounters() {
        EcstaticBeauty card = new EcstaticBeauty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    private ProfaneTutor suspendProfaneTutor() {
        ProfaneTutor card = new ProfaneTutor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void castBeauty() {
        harness.setHand(player1, List.of(new EcstaticBeauty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
