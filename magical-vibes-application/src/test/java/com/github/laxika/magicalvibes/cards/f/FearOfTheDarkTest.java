package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfTheDark.class})
class FearOfTheDarkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with no Glimmer creatures defending grants menace and deathtouch")
    void grantsMenaceAndDeathtouchWhenDefenderHasNoGlimmers() {
        Permanent fear = addCreatureReady(player1, new FearOfTheDark());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, fear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, fear, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A defending Glimmer creature prevents the attack trigger")
    void doesNotGrantKeywordsWhenDefenderControlsGlimmerCreature() {
        Permanent fear = addCreatureReady(player1, new FearOfTheDark());
        addCreatureReady(player2, glimmerCreature());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, fear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, fear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The condition is checked again before the attack trigger resolves")
    void conditionIsRecheckedBeforeResolution() {
        Permanent fear = addCreatureReady(player1, new FearOfTheDark());

        declareAttackers(List.of(0));
        addCreatureReady(player2, glimmerCreature());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, fear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, fear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent fear = addCreatureReady(player1, new FearOfTheDark());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, fear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, fear, Keyword.DEATHTOUCH)).isFalse();
    }

    private static Card glimmerCreature() {
        Card card = new Card();
        card.setName("Glimmer");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.GLIMMER));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
