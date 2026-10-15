package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.MirrorImage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VivienReid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NicolBolasTheRavager.class, Shock.class, GreenwoodSentinel.class, VivienReid.class, MirrorImage.class})
class NicolBolasTheRavagerTest extends BaseCardTest {

    @Test
    void etbMakesEachOpponentDiscardOneCard() {
        Card discarded = new Shock();
        harness.setHand(player1, List.of(new NicolBolasTheRavager()));
        harness.setHand(player2, List.of(discarded));
        addManaForCreature();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void sorceryAbilityReturnsNicolBolasTransformed() {
        addReadyRavager();
        addManaForTransform();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    @Test
    void plusTwoDrawsTwoCards() {
        Permanent bolas = addReadyArisen();
        Card first = new Shock();
        Card second = new GreenwoodSentinel();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, indexOf(bolas), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void minusThreeDestroysAPlaneswalkerWithTenDamage() {
        Permanent bolas = addReadyArisen();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VivienReid());
        target.setCounterCount(CounterType.LOYALTY, 10);

        harness.activateAbility(player1, indexOf(bolas), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void minusFourReanimatesAPlaneswalkerFromAnyGraveyard() {
        Permanent bolas = addReadyArisen();
        Card target = new VivienReid();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, indexOf(bolas), 2, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void minusTwelveExilesAllButTheBottomCardOfTargetLibrary() {
        Permanent bolas = addReadyArisen();
        Card top = new Shock();
        Card middle = new GreenwoodSentinel();
        Card bottom = new Shock();
        harness.setLibrary(player2, List.of(top, middle, bottom));

        harness.activateAbility(player1, indexOf(bolas), 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottom);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(top, middle)
                .doesNotContain(bottom);
    }

    @Test
    void singleFacedCopyIsExiledAndDoesNotReturnTransformed() {
        Permanent original = addReadyRavager();
        Card mirror = new MirrorImage();
        harness.setHand(player1, List.of(mirror));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(mirror.getId()))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, copy.getId());
        resolveAllTriggers();
        addManaForTransform();

        harness.activateAbility(player1, indexOf(copy), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(mirror);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(mirror.getId()));
    }

    @Test
    void transformReturnsStolenRavagerToItsOwnerAsANewPermanent() {
        NicolBolasTheRavager card = new NicolBolasTheRavager();
        card.setOwnerId(player2.getId());
        Permanent ravager = addCreatureReady(player1, card);
        prepareMainPhase();
        ravager.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ravager.setMarkedDamage(1);
        ravager.tap();
        addManaForTransform();

        harness.activateAbility(player1, indexOf(ravager), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ravager);
        Permanent returned = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(returned.getId()).isNotEqualTo(ravager.getId());
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    void transformCannotBeActivatedOutsideAMainPhase() {
        addReadyRavager();
        addManaForTransform();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();
    }

    @Test
    void transformCannotBeActivatedDuringOpponentsTurn() {
        addReadyRavager();
        addManaForTransform();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformCannotBeActivatedWithASpellOnTheStack() {
        addReadyRavager();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        addManaForTransform();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newlyReturnedPlaneswalkerCanActivateLoyaltyImmediately() {
        addReadyRavager();
        addManaForTransform();
        Card first = new Shock();
        Card second = new GreenwoodSentinel();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(9);
    }

    @Test
    void minusThreeDealsTenDamageToCreatureAndPaysThreeLoyalty() {
        Permanent bolas = addReadyArisen();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, indexOf(bolas), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getOriginalCard());
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(17);
    }

    @Test
    void minusFourReturnsCreatureFromOwnGraveyardUntapped() {
        Permanent bolas = addReadyArisen();
        Card target = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, indexOf(bolas), 2, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(target.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(16);
    }

    @Test
    void minusFourCannotTargetAnInstantCard() {
        Permanent bolas = addReadyArisen();
        Card target = new Shock();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(bolas), 2,
                null, target.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
    }

    @Test
    void ultimateCanTargetControllersOwnOneCardLibrary() {
        Permanent bolas = addReadyArisen();
        Card bottom = new Shock();
        harness.setLibrary(player1, List.of(bottom));

        harness.activateAbility(player1, indexOf(bolas), 3, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bottom);
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    void ultimateDoesNothingToEmptyLibrary() {
        Permanent bolas = addReadyArisen();
        harness.setLibrary(player2, List.of());
        List<Card> previouslyExiled = List.copyOf(gd.getPlayerExiledCards(player2.getId()));

        harness.activateAbility(player1, indexOf(bolas), 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(previouslyExiled);
    }

    @Test
    void opponentChoosesExactlyOneDiscardAndControllerKeepsTheirHand() {
        Card keptByController = new Shock();
        Card keptByOpponent = new GreenwoodSentinel();
        Card discarded = new Shock();
        harness.setHand(player1, List.of(new NicolBolasTheRavager(), keptByController));
        harness.setHand(player2, List.of(keptByOpponent, discarded));
        addManaForCreature();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptByController);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void entryTriggerResolvesWhenOpponentHasNoCards() {
        harness.setHand(player1, List.of(new NicolBolasTheRavager()));
        harness.setHand(player2, List.of());
        addManaForCreature();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyRavager() {
        Permanent permanent = addCreatureReady(player1, new NicolBolasTheRavager());
        prepareMainPhase();
        return permanent;
    }

    private Permanent addReadyArisen() {
        NicolBolasTheRavager card = new NicolBolasTheRavager();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setCounterCount(CounterType.LOYALTY, 20);
        permanent.setSummoningSick(false);
        prepareMainPhase();
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void addManaForCreature() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addManaForTransform() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
