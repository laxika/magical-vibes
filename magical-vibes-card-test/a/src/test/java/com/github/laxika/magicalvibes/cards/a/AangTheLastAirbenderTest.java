package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        AangTheLastAirbender.class,
        GrizzlyBears.class,
        ExpandedAnatomy.class,
        Island.class
})
class AangTheLastAirbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Airbends another nonland permanent and lets its owner cast it for {2}")
    void airbendsPermanentForGenericAlternativeCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangTheLastAirbender()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(bears.getOriginalCard().getId())).isEqualTo(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, bears.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The airbend target must be another nonland permanent")
    void airbendRejectsLandTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new AangTheLastAirbender()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("Aang can enter without choosing a target")
    void canEnterWithoutTarget() {
        harness.castFromHand(player1, new AangTheLastAirbender(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aang, the Last Airbender");
    }

    @Test
    @DisplayName("Casting a Lesson gives Aang lifelink until end of turn")
    void lessonSpellGrantsLifelink() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangTheLastAirbender());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, aang.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aang, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aang, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Airbend can exile another permanent controlled by Aang's controller")
    void airbendsOwnPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangTheLastAirbender()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(bears.getOriginalCard().getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting a non-Lesson spell does not give Aang lifelink")
    void nonLessonDoesNotGrantLifelink() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangTheLastAirbender());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aang, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Lesson does not give Aang lifelink")
    void opponentsLessonDoesNotGrantLifelink() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangTheLastAirbender());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ExpandedAnatomy()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, bears.getId());

        assertThat(gqs.hasKeyword(gd, aang, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Aang gains lifelink before the Lesson resolves even when it targets another creature")
    void lessonTriggerResolvesIndependentlyOfLessonTarget() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangTheLastAirbender());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, bears.getId());
        assertThat(gqs.hasKeyword(gd, aang, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aang, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
