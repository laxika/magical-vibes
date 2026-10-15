package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessedGoat.class})
class PossessedGoatTest extends BaseCardTest {

    @Test
    @DisplayName("Discard is paid before resolution and tapped Goats can activate")
    void discardIsCostAndTappedGoatCanActivate() {
        Permanent goat = harness.addToBattlefieldAndReturn(player1, new PossessedGoat());
        goat.setSummoningSick(true);
        goat.tap();
        harness.setHand(player1, List.of(new PossessedGoat(), new PossessedGoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Possessed Goat");
        assertThat(gqs.getEffectivePower(gd, goat)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, goat)).doesNotContain(CardColor.BLACK);
        assertThat(gqs.hasEffectiveSubtype(gd, goat, CardSubtype.DEMON)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goat)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, goat)).contains(CardColor.WHITE, CardColor.BLACK);
        assertThat(gqs.hasEffectiveSubtype(gd, goat, CardSubtype.DEMON)).isTrue();
    }

    @Test
    @DisplayName("Separate Goats each get one activation")
    void activationLimitIsPerPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PossessedGoat());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PossessedGoat());
        harness.setHand(player1, List.of(new PossessedGoat(), new PossessedGoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.DEMON)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets three +1/+1 counters and becomes a black Demon")
    void activationAddsCountersAndGrantsBlackDemon() {
        Permanent goat = harness.addToBattlefieldAndReturn(player1, new PossessedGoat());
        harness.setHand(player1, List.of(new PossessedGoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, goat)).contains(CardColor.WHITE, CardColor.BLACK);
        assertThat(gqs.hasEffectiveSubtype(gd, goat, CardSubtype.GOAT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, goat, CardSubtype.DEMON)).isTrue();
        harness.assertInGraveyard(player1, "Possessed Goat");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new PossessedGoat());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a second time")
    void cannotActivateTwice() {
        harness.addToBattlefield(player1, new PossessedGoat());
        harness.setHand(player1, List.of(new PossessedGoat(), new PossessedGoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
