package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoothaMasteringTheMoment.class, Divination.class, Forest.class, GrizzlyBears.class, ThinkTwice.class, EntrancingMelody.class})
class RoothaMasteringTheMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying and hasty Elemental sized by the greatest instant or sorcery mana value")
    void createsTokenSizedByGreatestSpellManaValue() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player1, List.of(new ThinkTwice(), new Divination()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.HASTE);
    }

    @Test
    @DisplayName("Does not trigger without an instant or sorcery cast this turn")
    void doesNotTriggerWithoutMatchingSpell() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Includes the chosen X in a resolved spell's mana value")
    void includesChosenXInManaValue() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2, bears.getId());
        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts a spell cast before Rootha entered the battlefield")
    void countsSpellCastBeforeRoothaEntered() {
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();
        harness.castAndResolveInstant(player1, 0);
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count an opponent's instant spell")
    void doesNotCountOpponentSpell() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat even after casting an instant")
    void doesNotTriggerDuringOpponentCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();
        harness.castAndResolveInstant(player1, 0);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Counts a flashback spell using its mana cost rather than its flashback cost")
    void countsFlashbackSpellUsingManaCost() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();

        harness.castAndResolveFlashback(player1, 0, null);
        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting the first qualifying spell after combat begins is too late to trigger")
    void doesNotTriggerForFirstSpellCastDuringCombat() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new Forest()));
        advanceToBeginningOfCombat();
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
