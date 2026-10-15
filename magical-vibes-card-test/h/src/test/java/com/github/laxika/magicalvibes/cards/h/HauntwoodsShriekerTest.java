package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntwoodsShrieker.class, Forest.class})
class HauntwoodsShriekerTest extends BaseCardTest {

    @Test
    void attackingManifestsDread() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Card manifestedCard = new HauntwoodsShrieker();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shrieker)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void revealsCreatureAndMayTurnItFaceUpForFree() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HauntwoodsShrieker());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        int remainingMana = gd.playerManaPools.get(player1.getId()).getTotalAllMana();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(remainingMana);
    }

    @Test
    void mayTurnFaceUpChoiceCanBeDeclined() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HauntwoodsShrieker());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isFaceDown()).isTrue();
    }

    @Test
    void nonCreatureFaceDownPermanentCanBeTargetedWithoutMayChoice() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        assertThat(target.isFaceDown()).isTrue();
    }

    @Test
    void attackingWithOneCardInLibraryManifestsThatCardWithoutMilling() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shrieker)));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown()
                        && permanent.getOriginalCard().getId().equals(card.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void attackingWithEmptyLibraryDoesNothing() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shrieker)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shrieker);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canManifestLandAndOnlyLooksAtControllersTopTwoCards() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Card creature = new HauntwoodsShrieker();
        Card land = new Forest();
        Card third = new Forest();
        Card opponentCard = new Forest();
        harness.setLibrary(player1, List.of(creature, land, third));
        harness.setLibrary(player2, List.of(opponentCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shrieker)));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown()
                        && permanent.getOriginalCard().getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void attackTriggerResolvesAfterShriekerLeavesBattlefield() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Card card = new HauntwoodsShrieker();
        Card other = new Forest();
        harness.setLibrary(player1, List.of(card, other));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shrieker)));
        gd.playerBattlefields.get(player1.getId()).remove(shrieker);
        harness.setGraveyard(player1, List.of(shrieker.getCard()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getOriginalCard().getId().equals(card.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    void cannotTargetFaceUpPermanent() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HauntwoodsShrieker());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(shrieker), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetTurnedFaceUpInResponseDoesNotOfferChoice() {
        Permanent shrieker = addCreatureReady(player1, new HauntwoodsShrieker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HauntwoodsShrieker());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        target.setManifested(true);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(target));
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickShriekerCanTurnOwnPermanentFaceUp() {
        Permanent shrieker = harness.addToBattlefieldAndReturn(player1, new HauntwoodsShrieker());
        shrieker.setSummoningSick(true);
        shrieker.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HauntwoodsShrieker());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        target.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker),
                null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isFaceDown()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
