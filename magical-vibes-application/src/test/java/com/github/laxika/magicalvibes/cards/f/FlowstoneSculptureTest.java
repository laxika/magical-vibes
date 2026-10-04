package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FlowstoneSculpture.class, Forest.class, Island.class, Capsize.class})
class FlowstoneSculptureTest extends BaseCardTest {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on this creature.";
    private static final String FLYING_MODE = "This creature gains flying.";
    private static final String FIRST_STRIKE_MODE = "This creature gains first strike.";
    private static final String TRAMPLE_MODE = "This creature gains trample.";

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on it and the discard cost is paid")
    void counterMode() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(sculpture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Discard cost lets the player choose which card to discard")
    void discardsChosenCard() {
        Permanent sculpture = addSculpture();
        Island kept = new Island();
        Forest discarded = new Forest();

        activate(1, kept, discarded);
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(sculpture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying mode grants flying and it lasts past end of turn")
    void flyingModeLastsIndefinitely() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, FLYING_MODE);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("First strike mode grants first strike")
    void firstStrikeMode() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, FIRST_STRIKE_MODE);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample mode grants trample")
    void trampleMode() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An unknown mode label is rejected")
    void illegalModeRejected() {
        addSculpture();

        activate();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "This creature gains haste."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot be activated with an empty hand")
    void requiresACardToDiscard() {
        addSculpture();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations accumulate counters and all three keywords")
    void repeatedActivationsAccumulate() {
        Permanent sculpture = addSculpture();
        Permanent other = addCreatureReady(player2, new FlowstoneSculpture());

        for (String choice : List.of(COUNTER_MODE, COUNTER_MODE, FLYING_MODE,
                FIRST_STRIKE_MODE, TRAMPLE_MODE)) {
            activate();
            harness.handleListChoice(player1, choice);
        }

        assertThat(sculpture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(6);
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE)) {
            assertThat(gqs.hasKeyword(gd, sculpture, keyword)).isTrue();
            assertThat(gqs.hasKeyword(gd, other, keyword)).isFalse();
        }
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE)) {
            assertThat(gqs.hasKeyword(gd, sculpture, keyword)).isTrue();
        }
        assertThat(sculpture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void doesNotRequireTapOrHaste() {
        Permanent sculpture = harness.addToBattlefieldAndReturn(player1, new FlowstoneSculpture());
        sculpture.setSummoningSick(true);
        sculpture.setTapped(true);

        activate();
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(sculpture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sculpture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activation requires two mana and does not discard on an unaffordable attempt")
    void requiresTwoMana() {
        addSculpture();
        Island card = new Island();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gained keywords and counters do not follow the card after it leaves and returns")
    void enhancementsDoNotSurviveLeavingBattlefield() {
        Permanent sculpture = addSculpture();
        activate();
        harness.handleListChoice(player1, FLYING_MODE);
        activate();
        harness.handleListChoice(player1, COUNTER_MODE);

        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sculpture.getId());
        harness.assertNotOnBattlefield(player1, "Flowstone Sculpture");
        harness.assertInHand(player1, "Flowstone Sculpture");

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Flowstone Sculpture");
        assertThat(returned.getId()).isNotEqualTo(sculpture.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSculpture() {
        return addCreatureReady(player1, new FlowstoneSculpture());
    }

    /** Pays {2} and the discard cost, then resolves the ability up to the mode prompt. */
    private void activate() {
        activate(0, new Island());
    }

    private void activate(int discardCardIndex, Card... handCards) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(handCards));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, discardCardIndex);
        harness.passBothPriorities();
    }
}
