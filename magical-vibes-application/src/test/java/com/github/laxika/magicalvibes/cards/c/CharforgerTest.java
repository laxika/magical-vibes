package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MyrCustodian;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Charforger.class, Cankerbloom.class, PropheticPrism.class, MyrCustodian.class, Forest.class})
class CharforgerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Phyrexian Goblin token when it enters")
    void createsPhyrexianGoblinTokenOnEntry() {
        harness.castFromHand(player1, new Charforger(), "{1}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Phyrexian Goblin");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Gets oil counters when a creature or artifact you control dies")
    void getsOilCountersWhenOwnCreatureOrArtifactDies() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cankerbloom());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        putIntoGraveyard(creature);
        putIntoGraveyard(artifact);

        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores an opponent's permanent and a noncreature nonartifact permanent")
    void ignoresOpponentPermanentAndNoncreatureNonartifact() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Cankerbloom());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);

        assertThat(charforger.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Removes three oil counters and exiles the top card for play this turn")
    void removesOilCountersAndExilesTopCard() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Card top = putCardOnTopOfLibrary();
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(charforger.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(top.getId()));
        harness.passBothPriorities();

        assertThat(charforger.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("Cannot activate without three oil counters")
    void cannotActivateWithoutThreeOilCounters() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsArtifactCreatureOnlyOnce() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new MyrCustodian());

        putIntoGraveyard(artifactCreature);

        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void countsDeathOfItsGoblinToken() {
        harness.castFromHand(player1, new Charforger(), "{1}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent charforger = findPermanent(player1, "Charforger");

        putIntoGraveyard(findPermanent(player1, "Phyrexian Goblin"));

        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void countsControlledCreatureOwnedByOpponent() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Cankerbloom card = new Cankerbloom();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);

        putIntoGraveyard(creature);

        harness.assertInGraveyard(player2, "Cankerbloom");
        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void ignoresOwnedCreatureControlledByOpponent() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Cankerbloom card = new Cankerbloom();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);

        putIntoGraveyard(creature);

        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(charforger.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void countsLandThatWasCreatureOnBattlefield() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        Permanent animatedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        animatedLand.getGrantedCardTypes().add(CardType.CREATURE);
        animatedLand.setAnimatedUntilEndOfTurn(true);
        animatedLand.setAnimatedPower(3);
        animatedLand.setAnimatedToughness(3);

        putIntoGraveyard(animatedLand);

        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void canCastExiledCreatureOnlyAfterPayingItsCost() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Card top = putCardOnTopOfLibrary();
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cankerbloom");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(top.getId()));
    }

    @Test
    void canPlayExiledLand() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(land.getId()));
    }

    @Test
    void activationWithEmptyLibraryStillPaysCounters() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 4);
        harness.setLibrary(player1, List.of());
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(charforger.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledCreatureStillRequiresNormalTiming() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Card top = putCardOnTopOfLibrary();
        enterMainWithPriority(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
    }

    @Test
    void cannotPlayExiledCardOnFollowingTurn() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Card top = putCardOnTopOfLibrary();
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        enterMainWithPriority(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
    }

    @Test
    void abilityResolvesAfterCharforgerLeavesBattlefield() {
        Permanent charforger = harness.addToBattlefieldAndReturn(player1, new Charforger());
        charforger.setCounterCount(CounterType.OIL, 3);
        Card top = putCardOnTopOfLibrary();
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, charforger));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cankerbloom");
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private Card putCardOnTopOfLibrary() {
        Card card = new Cankerbloom();
        harness.setLibrary(player1, List.of(card));
        return card;
    }

    private void enterMainWithPriority(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
