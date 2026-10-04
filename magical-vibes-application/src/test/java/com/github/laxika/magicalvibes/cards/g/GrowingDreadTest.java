package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PatchworkBeastie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrowingDread.class, Forest.class, PatchworkBeastie.class})
class GrowingDreadTest extends BaseCardTest {

    @Test
    void manifestsDreadAndPutsCounterOnPermanentTurnedFaceUp() {
        Card manifestedCard = new PatchworkBeastie();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new GrowingDread()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = findPermanent(player1, "Patchwork Beastie");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canBeCastDuringOpponentsUpkeepWithAnEmptyLibrary() {
        gd.activePlayerId = player2.getId();
        gd.currentStep = TurnStep.UPKEEP;
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GrowingDread()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Growing Dread");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void manifestsTheOnlyCardWithoutPuttingAnythingInGraveyard() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new GrowingDread()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        Permanent manifested = findPermanent(player1, "Forest");
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canManifestTheSecondCardButCannotTurnANoncreatureFaceUpForItsManaCost() {
        Card topCard = new PatchworkBeastie();
        Card secondCard = new GrowingDread();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new GrowingDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(secondCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachCopyTriggersAndTheCounterWaitsForResolution() {
        harness.addToBattlefield(player1, new GrowingDread());
        harness.addToBattlefield(player1, new GrowingDread());
        Permanent manifested = harness.addToBattlefieldAndReturn(player1, new PatchworkBeastie());
        manifested.setManifested(true);
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, 2);

        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenOpponentTurnsAPermanentFaceUp() {
        harness.addToBattlefield(player1, new GrowingDread());
        Permanent manifested = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        manifested.setManifested(true);
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.turnFaceUp(player2, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterTriggerResolvesAfterGrowingDreadLeavesTheBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrowingDread());
        Permanent manifested = harness.addToBattlefieldAndReturn(player1, new PatchworkBeastie());
        manifested.setManifested(true);
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, 1);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
