package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DogmeatEverLoyal.class, Bonesplitter.class, GrizzlyBears.class, HolyStrength.class})
class DogmeatEverLoyalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling five and returning an Aura or Equipment")
    void millsFiveAndReturnsAuraOrEquipment() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DogmeatEverLoyal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(5)
                .doesNotContain(aura);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
    }

    @Test
    @DisplayName("Creates Junk when an enchanted or equipped creature attacks")
    void createsJunkForEnchantedOrEquippedAttackers() {
        addCreatureReady(player1, new DogmeatEverLoyal());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(enchanted.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void returnsEquipmentMilledByTheSameAbility() {
        Card equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(equipment, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        castDogmeatAndResolveTriggers();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).doesNotContain(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void millsRemainingCardsWithoutReturningAnIneligibleCard() {
        Card creature = new GrizzlyBears();
        Card opponentsAura = new HolyStrength();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentsAura));
        harness.setLibrary(player1, List.of(creature));

        castDogmeatAndResolveTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsAura);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnsAnAuraEvenWhenTheLibraryIsEmpty() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of());

        castDogmeatAndResolveTriggers();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void dogmeatCreatesOnlyOneJunkWhenBothEnchantedAndEquipped() {
        Permanent dogmeat = addCreatureReady(player1, new DogmeatEverLoyal());
        harness.addToBattlefieldAndReturn(player2, new HolyStrength()).setAttachedTo(dogmeat.getId());
        harness.addToBattlefieldAndReturn(player1, new Bonesplitter()).setAttachedTo(dogmeat.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        assertThat(countPermanents(player2, "Junk")).isZero();
    }

    @Test
    void opponentsEnchantedAttackerDoesNotCreateJunk() {
        addCreatureReady(player1, new DogmeatEverLoyal());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new HolyStrength()).setAttachedTo(attacker.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isZero();
        assertThat(countPermanents(player2, "Junk")).isZero();
    }

    @Test
    void junkSacrificesAsACostAndAllowsCastingTheExiledCardWithMana() {
        Permanent junk = createJunk();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);

        harness.activateAbility(player1, junkIndex, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(junk);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void junkCannotBeActivatedDuringCombat() {
        Permanent junk = createJunk();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(junk);
        assertThat(junk.isTapped()).isFalse();
    }

    @Test
    void junkCannotBeActivatedWhileTapped() {
        Permanent junk = createJunk();
        junk.tap();
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(junk);
    }

    @Test
    void junkWithAnEmptyLibraryIsStillSacrificed() {
        Permanent junk = createJunk();
        harness.setLibrary(player1, List.of());
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);

        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(junk);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void junkPlayPermissionExpiresAtEndOfTurn() {
        Permanent junk = createJunk();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears()));
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);
        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    private void castDogmeatAndResolveTriggers() {
        harness.setHand(player1, List.of(new DogmeatEverLoyal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent createJunk() {
        Permanent dogmeat = addCreatureReady(player1, new DogmeatEverLoyal());
        harness.addToBattlefieldAndReturn(player1, new Bonesplitter()).setAttachedTo(dogmeat.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Junk");
    }
}
