package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KoalaSheep;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireNationSalvagers.class, KoalaSheep.class, MobilizerMech.class})
class FireNationSalvagersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a +1/+1 counter on target creature you control")
    void enteringPutsCounterOnTargetCreature() {
        Permanent target = addCreatureReady(player1, new KoalaSheep());
        castSalvagers(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability cannot target an opponent's creature")
    void enteringCannotTargetOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new KoalaSheep());
        harness.setHand(player1, List.of(new FireNationSalvagers()));
        addSalvagerMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle you control");
    }

    @Test
    @DisplayName("Combat damage from a creature with a counter returns a creature or Vehicle from the damaged player's graveyard")
    void counteredCreatureCombatDamageReturnsVehicle() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player2, List.of(vehicle));

        addCreatureReady(player1, new FireNationSalvagers());
        Permanent attacker = addCreatureReady(player1, new KoalaSheep());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(vehicle.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(vehicle.getId()));
    }

    @Test
    @DisplayName("Combat damage from a creature without counters does not trigger")
    void creatureWithoutCountersDoesNotTrigger() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player2, List.of(vehicle));

        addCreatureReady(player1, new FireNationSalvagers());
        Permanent attacker = addCreatureReady(player1, new KoalaSheep());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(vehicle);
    }

    @Test
    @DisplayName("Entering can put a counter on an uncrewed Vehicle")
    void enteringPutsCounterOnUncrewedVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new MobilizerMech());

        castSalvagers(vehicle.getId());

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning a creature does not mill the damaged player")
    void combatTriggerDoesNotMill() {
        Card target = new KoalaSheep();
        Card libraryCard = new KoalaSheep();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(libraryCard));
        Permanent salvagers = addCreatureReady(player1, new FireNationSalvagers());
        salvagers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        salvagers.setAttacking(true);

        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two qualifying attackers produce only one reanimation trigger")
    void simultaneousAttackersTriggerOnce() {
        Card first = new MobilizerMech();
        Card second = new MobilizerMech();
        harness.setGraveyard(player2, List.of(first, second));
        addCreatureReady(player1, new FireNationSalvagers());
        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player1, new KoalaSheep());
            attacker.setCounterCount(CounterType.CHARGE, 1);
            attacker.setAttacking(true);
        }

        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
    }

    @Test
    @DisplayName("Only cards in the damaged player's graveyard can be chosen")
    void excludesControllersGraveyard() {
        Card ownCreature = new KoalaSheep();
        Card target = new MobilizerMech();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(target));
        Permanent salvagers = addCreatureReady(player1, new FireNationSalvagers());
        salvagers.setCounterCount(CounterType.CHARGE, 1);
        salvagers.setAttacking(true);

        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(target);
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution is not returned")
    void missingTargetIsNotReturned() {
        Card target = new MobilizerMech();
        harness.setGraveyard(player2, List.of(target));
        Permanent salvagers = addCreatureReady(player1, new FireNationSalvagers());
        salvagers.setCounterCount(CounterType.CHARGE, 1);
        salvagers.setAttacking(true);

        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    private void castSalvagers(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FireNationSalvagers()));
        addSalvagerMana();
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();
    }

    private void addSalvagerMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
