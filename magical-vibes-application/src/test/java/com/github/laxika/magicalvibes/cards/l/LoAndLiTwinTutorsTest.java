package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        LoAndLiTwinTutors.class,
        DiabolicTutor.class,
        ExpandedAnatomy.class,
        GrizzlyBears.class,
        Shock.class
})
class LoAndLiTwinTutorsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Lesson and Noble cards only")
    void etbOffersLessonAndNobleCards() {
        LoAndLiTwinTutors loAndLi = new LoAndLiTwinTutors();
        ExpandedAnatomy lesson = new ExpandedAnatomy();
        GrizzlyBears noble = new GrizzlyBears();
        noble.setSubtypes(List.of(CardSubtype.NOBLE));
        DiabolicTutor nonMatchingSpell = new DiabolicTutor();
        GrizzlyBears nonMatchingCreature = new GrizzlyBears();

        harness.setHand(player1, List.of(loAndLi));
        harness.setLibrary(player1, List.of(nonMatchingSpell, lesson, nonMatchingCreature, noble));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).containsExactlyInAnyOrder(lesson, noble);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(offered.indexOf(lesson)));

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Noble creatures you control have lifelink")
    void nobleCreaturesHaveLifelink() {
        harness.addToBattlefield(player1, new LoAndLiTwinTutors());
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(noble).setSubtypes(List.of(CardSubtype.NOBLE));
        Permanent nonNoble = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, noble, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonNoble, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Only Lesson spells have lifelink")
    void onlyLessonSpellsHaveLifelink() {
        harness.addToBattlefield(player1, new LoAndLiTwinTutors());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        ExpandedAnatomy lesson = new ExpandedAnatomy();
        harness.setHand(player1, List.of(lesson));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
        StackEntry lessonEntry = gd.stack.getLast();
        assertThat(gqs.shouldControllerSpellHaveLifelink(gd, lessonEntry)).isTrue();
        harness.passBothPriorities();

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        StackEntry shockEntry = gd.stack.getLast();
        assertThat(gqs.shouldControllerSpellHaveLifelink(gd, shockEntry)).isFalse();
    }
}
