package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AudaciousKnuckleblade.class, GrizzlyBears.class})
@DisplayName("Audacious Knuckleblade")
class AudaciousKnucklebladeTest extends BaseCardTest {

    @Test
    @DisplayName("The green Exhaust ability seeks a tapped copy onto the battlefield")
    void greenExhaustAbilitySeeksTappedCopy() {
        harness.addToBattlefield(player1, new AudaciousKnuckleblade());
        Card sought = new AudaciousKnuckleblade();
        harness.setLibrary(player1, List.of(sought));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == sought)
                .findFirst()
                .orElseThrow();
        assertThat(entered.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The blue Exhaust ability surveils two cards and untaps the source")
    void blueExhaustAbilitySurveilsAndUntapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AudaciousKnuckleblade());
        source.tap();
        Card top = new GrizzlyBears();
        Card second = new AudaciousKnuckleblade();
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, second);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The red Exhaust ability gives all creatures you control haste until end of turn")
    void redExhaustAbilityGrantsHasteToAllOwnCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AudaciousKnuckleblade());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(source.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponent.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Each Exhaust ability can be activated only once")
    void eachExhaustAbilityCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new AudaciousKnuckleblade());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
