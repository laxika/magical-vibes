package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StudiousFirstYearRampantGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Studious First-Year enters prepared with a Rampant Growth copy in exile")
    void entersPrepared() {
        Permanent student = castStudent();

        assertThat(student.isPrepared()).isTrue();
        UUID copyId = student.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting the prepared Rampant Growth copy unprepares Studious First-Year and fetches a tapped Forest")
    void castingPrepareCopyFetchesTappedBasicLand() {
        Permanent student = castStudent();
        UUID copyId = student.getPreparedSpellCardId();
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.add(new Forest());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(student.isPrepared()).isFalse();
        assertThat(student.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
    }

    private Permanent castStudent() {
        harness.setHand(player1, List.of(new StudiousFirstYearRampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Studious First-Year");
    }
}
