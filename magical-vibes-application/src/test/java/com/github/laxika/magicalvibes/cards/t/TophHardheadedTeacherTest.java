package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TophHardheadedTeacher.class, Shock.class, GrizzlyBears.class,
        Forest.class, FirebendingLesson.class, HillGiant.class})
class TophHardheadedTeacherTest extends BaseCardTest {

    @Test
    void entersMayDiscardToReturnInstantOrSorcery() {
        Card shock = new Shock();
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(discard));
        harness.setGraveyard(player1, List.of(shock));

        harness.enterBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    void castingNonLessonEarthbendsOne() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new TophHardheadedTeacher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    void castingLessonEarthbendsWithAdditionalCounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new TophHardheadedTeacher());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }
}
