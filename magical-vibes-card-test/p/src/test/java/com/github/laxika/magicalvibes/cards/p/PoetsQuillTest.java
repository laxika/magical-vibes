package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoetsQuill.class, EnvironmentalSciences.class, EagerFirstYear.class})
class PoetsQuillTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, Learn searches for a Lesson with an empty hand")
    void enterLearnSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        harness.setHand(player1, List.of(new PoetsQuill()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("When it enters, Learn can discard and draw")
    void enterLearnDiscardsAndDraws() {
        Card discarded = new EagerFirstYear();
        Card drawn = new EagerFirstYear();
        harness.setHand(player1, List.of(new PoetsQuill(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Equipping a creature gives it +1/+1 and lifelink")
    void equippingCreatureBoostsAndGrantsLifelink() {
        Permanent quill = harness.addToBattlefieldAndReturn(player1, new PoetsQuill());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(quill.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void learnCanDeclineBothOptions() {
        Card retained = new EagerFirstYear();
        Card lesson = new EnvironmentalSciences();
        Card undrawn = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new PoetsQuill(), retained));
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void learnWithNoHandOrLessonsDoesNothing() {
        harness.setHand(player1, List.of(new PoetsQuill()));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Poet's Quill");
    }

    @Test
    void reequippingMovesBothBonusesToNewCreature() {
        Permanent quill = harness.addToBattlefieldAndReturn(player1, new PoetsQuill());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(quill.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent quill = harness.addToBattlefieldAndReturn(player1, new PoetsQuill());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quill.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresBlackMana() {
        Permanent quill = harness.addToBattlefieldAndReturn(player1, new PoetsQuill());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quill.getAttachedTo()).isNull();
    }

    @Test
    void equippedCreatureGainsLifeFromCombatDamage() {
        harness.addToBattlefield(player1, new PoetsQuill());
        Permanent creature = addCreatureReady(player1, new EagerFirstYear());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
