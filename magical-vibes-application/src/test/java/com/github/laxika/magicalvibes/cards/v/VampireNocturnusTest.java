package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.Divination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireNocturnus.class, VampireAristocrat.class, RuneclawBear.class, Divination.class})
class VampireNocturnusTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Vampire Nocturnus puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VampireNocturnus()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vampire Nocturnus");
    }

    @Test
    @DisplayName("Resolving puts Vampire Nocturnus onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VampireNocturnus()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Vampire Nocturnus");
    }

    @Test
    @DisplayName("Buffs self when top card of library is black")
    void buffsSelfWhenTopCardIsBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        // Put a black card on top of library
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent nocturnus = findPermanent(player1, "Vampire Nocturnus");

        // 3/3 base + 2/1 from effect = 5/4
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not buff self when top card of library is not black")
    void doesNotBuffSelfWhenTopCardIsNotBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        // Put a green card on top of library
        harness.setLibrary(player1, List.of(new RuneclawBear()));

        Permanent nocturnus = findPermanent(player1, "Vampire Nocturnus");

        // 3/3 base, no bonus
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not buff self when library is empty")
    void doesNotBuffSelfWhenLibraryIsEmpty() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.setLibrary(player1, List.of());

        Permanent nocturnus = findPermanent(player1, "Vampire Nocturnus");

        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Buffs other Vampire creatures when top card is black")
    void buffsOtherVampiresWhenTopCardIsBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent aristocrat = findPermanent(player1, "Vampire Aristocrat");

        // 2/2 base + 2/1 from Nocturnus = 4/3
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not buff other Vampires when top card is not black")
    void doesNotBuffOtherVampiresWhenTopCardIsNotBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new RuneclawBear()));

        Permanent aristocrat = findPermanent(player1, "Vampire Aristocrat");

        // 2/2 base, no bonus
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Vampire creatures even when top card is black")
    void doesNotBuffNonVampires() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent bears = findPermanent(player1, "Runeclaw Bear");

        // 2/2 base, no bonus (not a Vampire)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Vampire creatures")
    void doesNotBuffOpponentVampires() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player2, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent opponentVampire = findPermanent(player2, "Vampire Aristocrat");

        // 2/2 base, no bonus (opponent's creature, scope is OWN_CREATURES)
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentVampire, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Buff toggles dynamically when top card changes")
    void buffTogglesDynamicallyWhenTopCardChanges() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());

        // Start with a black card on top
        harness.setLibrary(player1, List.of(new VampireAristocrat(), new RuneclawBear()));

        Permanent aristocrat = findPermanent(player1, "Vampire Aristocrat");
        Permanent nocturnus = findPermanent(player1, "Vampire Nocturnus");

        // Black card on top: buff active
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(5);

        // Remove top card (simulating draw), now green card is on top
        gd.playerDecks.get(player1.getId()).removeFirst();

        // Non-black card on top: buff inactive
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Vampire Nocturnus leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent aristocrat = findPermanent(player1, "Vampire Aristocrat");

        // Verify buff is applied
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);

        // Remove Nocturnus
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Vampire Nocturnus"));

        // Bonus should be gone
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent aristocrat = findPermanent(player1, "Vampire Aristocrat");

        // Add temporary boost
        aristocrat.setPowerModifier(aristocrat.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(9); // 2 base + 5 spell + 2 static

        // Reset end-of-turn modifiers
        aristocrat.resetModifiers();

        // Static bonus still applied
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4); // 2 base + 2 static
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Buffs self even if Vampire subtype is removed (card names itself)")
    void buffsSelfEvenIfVampireSubtypeRemoved() {
        VampireNocturnus nocturnusCard = new VampireNocturnus();
        // Remove the Vampire subtype to simulate type-changing effects
        nocturnusCard.setSubtypes(List.of());
        harness.addToBattlefield(player1, nocturnusCard);
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        Permanent nocturnus = findPermanent(player1, "Vampire Nocturnus");

        // Per rulings: Vampire Nocturnus always buffs itself when condition is met,
        // even if it's no longer a Vampire (the card names itself in its text)
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(5); // 3 base + 2
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(4); // 3 base + 1
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isTrue();
    }

    @Test
    void revealsOnlyControllersTopCardToBothPlayersEvenWhenItIsNotBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        harness.setLibrary(player2, List.of(new VampireAristocrat()));
        harness.clearMessages();

        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[{")
                            && message.contains("Runeclaw Bear")
                            && message.contains("}],[]]"));
        }
    }

    @Test
    void stopsRevealingTopCardWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        }
    }

    @Test
    void revealsIntermediateTopCardDuringMultipleCardDraw() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        VampireAristocrat first = new VampireAristocrat();
        RuneclawBear second = new RuneclawBear();
        VampireNocturnus third = new VampireNocturnus();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.publishState();
        harness.clearMessages();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("Runeclaw Bear"));
    }

    @Test
    void multipleNocturnusBonusesStackExactlyOncePerSource() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));

        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            boolean nocturnus = permanent.getCard() instanceof VampireNocturnus;
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(nocturnus ? 7 : 6);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(nocturnus ? 5 : 4);
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    void otherVampireLosesBonusWhenItLosesItsCreatureTypes() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        harness.setLibrary(player1, List.of(new VampireAristocrat()));
        Permanent aristocrat = findPermanent(player1, "Vampire Aristocrat");
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);

        aristocrat.setLosesAllCreatureTypesUntilEndOfTurn(true);

        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
    }
}
