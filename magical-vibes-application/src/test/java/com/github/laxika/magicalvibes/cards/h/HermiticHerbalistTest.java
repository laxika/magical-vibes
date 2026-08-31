package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HermiticHerbalist.class, AirbendingLesson.class, GrizzlyBears.class})
class HermiticHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void firstAbilityAddsAnyColorMana() {
        addReadyHerbalist();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds two Lesson-only mana in any combination of colors")
    void secondAbilityAddsLessonOnlyManaInAnyCombination() {
        addReadyHerbalist();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOnlyManaForColor(Set.of(CardSubtype.LESSON), ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.getSubtypeSpellOnlyManaForColor(Set.of(CardSubtype.LESSON), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lesson-only mana can cast a Lesson spell")
    void lessonOnlyManaCanCastLessonSpell() {
        addReadyHerbalist();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.LESSON))).isZero();
    }

    @Test
    @DisplayName("Lesson-only mana cannot cast a non-Lesson spell")
    void lessonOnlyManaCannotCastNonLessonSpell() {
        addReadyHerbalist();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOnlyMana(Set.of(CardSubtype.LESSON), ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pool.getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.LESSON))).isEqualTo(2);
    }

    private void addReadyHerbalist() {
        Permanent herbalist = harness.addToBattlefieldAndReturn(player1, new HermiticHerbalist());
        herbalist.setSummoningSick(false);
    }
}
