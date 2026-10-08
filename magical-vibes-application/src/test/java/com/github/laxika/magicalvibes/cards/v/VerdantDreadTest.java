package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantDread.class, Forest.class, GrizzlyBears.class})
class VerdantDreadTest extends BaseCardTest {

    @Test
    void enteringManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void anotherVerdantDreadAlsoTriggersManifestDread() {
        harness.addToBattlefieldAndReturn(player1, new VerdantDread());
        Card firstManifested = new GrizzlyBears();
        Card secondManifested = new GrizzlyBears();
        Card firstGraveyard = new Forest();
        Card secondGraveyard = new Forest();
        harness.setLibrary(player1, List.of(firstManifested, firstGraveyard, secondManifested, secondGraveyard));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(firstManifested.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(secondManifested.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstGraveyard, secondGraveyard);
    }

    @Test
    void activationConjuresAnotherVerdantDreadOntoTheBattlefield() {
        harness.addToBattlefieldAndReturn(player1, new VerdantDread());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof VerdantDread)
                .hasSize(2);
    }

    @Test
    void enteringWithOneLibraryCardManifestsItWithoutPuttingAnythingInGraveyard() {
        Card onlyCard = new VerdantDread();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCard().getId()).isEqualTo(onlyCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentVerdantDreadDoesNotTriggerOurs() {
        harness.addToBattlefield(player1, new VerdantDread());
        Card ourLibraryCard = new VerdantDread();
        harness.setLibrary(player1, List.of(ourLibraryCard));
        harness.setLibrary(player2, List.of());

        harness.enterBattlefieldAndReturn(player2, new VerdantDread());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ourLibraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void differentlyNamedPermanentDoesNotTriggerManifestDread() {
        harness.addToBattlefield(player1, new VerdantDread());
        Card libraryCard = new VerdantDread();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void conjuredCopyTriggersBothCopiesToManifestDread() {
        harness.addToBattlefield(player1, new VerdantDread());
        Card first = new VerdantDread();
        Card second = new VerdantDread();
        Card firstRemainder = new VerdantDread();
        Card secondRemainder = new VerdantDread();
        harness.setLibrary(player1, List.of(first, firstRemainder, second, secondRemainder));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstRemainder, secondRemainder);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationIsRejectedOutsideMainPhase() {
        harness.addToBattlefield(player1, new VerdantDread());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void activationIsRejectedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new VerdantDread());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void activationIsRejectedWithAnotherAbilityOnStack() {
        harness.addToBattlefield(player1, new VerdantDread());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 0, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void manifestedVerdantDreadHasNoAbilityToTriggerWhenAnotherCopyEnters() {
        Card faceDownCopy = new VerdantDread();
        Card initialRemainder = new VerdantDread();
        harness.setLibrary(player1, List.of(faceDownCopy, initialRemainder));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(faceDownCopy.getId()));
        resolveAllTriggers();

        Card first = new VerdantDread();
        Card firstRemainder = new VerdantDread();
        Card second = new VerdantDread();
        Card secondRemainder = new VerdantDread();
        Card untouchedFirst = new VerdantDread();
        Card untouchedSecond = new VerdantDread();
        harness.setLibrary(player1, List.of(first, firstRemainder, second, secondRemainder,
                untouchedFirst, untouchedSecond));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouchedFirst, untouchedSecond);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested).hasSize(3);
    }
}
