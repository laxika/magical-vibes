package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StudiousFirstYearRampantGrowth.class, Forest.class})
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
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(student.isPrepared()).isFalse();
        assertThat(student.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
    }

    @Test
    @DisplayName("Studious First-Year is prepared immediately on entry without a triggered ability")
    void preparedImmediatelyOnEntry() {
        harness.castFromHand(player1, new StudiousFirstYearRampantGrowth(), "{G}");
        harness.passBothPriorities();
        Permanent student = findPermanent(player1, "Studious First-Year");

        assertThat(student.isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Failing to find still consumes preparation and shuffles the library")
    void failingToFindConsumesPreparation() {
        Permanent student = castStudent();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, student.getPreparedSpellCardId());
        assertThat(student.isPrepared()).isFalse();
        assertThat(student.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A prepared spell still requires its mana cost and a failed cast preserves preparation")
    void insufficientManaPreservesPreparation() {
        Permanent student = castStudent();
        UUID copyId = student.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(student.isPrepared()).isTrue();
        assertThat(student.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castStudent() {
        harness.castFromHand(player1, new StudiousFirstYearRampantGrowth(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Studious First-Year");
    }
}
