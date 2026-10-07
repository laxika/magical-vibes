package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemurWarShaman.class, GrizzlyBears.class, SuntailHawk.class})
class TemurWarShamanTest extends BaseCardTest {

    @Test
    void enteringManifestsTopCard() {
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TemurWarShaman()));
        harness.setLibrary(player1, List.of(topCard));
        addShamanMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.isFaceDown()
                        && permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void turnedUpCreatureMayFightOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent manifested = resolveShamanWithManifestedCreature();
        addGrizzlyBearsFaceUpMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(manifested);
    }

    @Test
    void decliningFaceUpFightLeavesOpponentCreatureAlive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent manifested = resolveShamanWithManifestedCreature();
        addGrizzlyBearsFaceUpMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void enteringWithEmptyLibraryDoesNotManifestAnything() {
        harness.setHand(player1, List.of(new TemurWarShaman()));
        harness.setLibrary(player1, List.of());
        addShamanMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void turningFaceUpWithNoOpponentCreatureDoesNotFightYourOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent manifested = resolveShamanWithManifestedCreature();
        addGrizzlyBearsFaceUpMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        resolveAllTriggers();

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, manifested);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(manifested.getMarkedDamage()).isZero();
    }

    @Test
    void fightDealsLethalDamageToBothCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent manifested = resolveShamanWithManifestedCreature();
        addGrizzlyBearsFaceUpMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(manifested);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(manifested.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void shamanTurningItselfFaceUpTriggersFightButDoesNotManifest() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new TemurWarShaman());
        shaman.setManifested(true);
        shaman.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Card topCard = new SuntailHawk();
        harness.setLibrary(player1, List.of(topCard));
        addShamanMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shaman);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent resolveShamanWithManifestedCreature() {
        harness.setHand(player1, List.of(new TemurWarShaman()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addShamanMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
    }

    private void addShamanMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    private void addGrizzlyBearsFaceUpMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
