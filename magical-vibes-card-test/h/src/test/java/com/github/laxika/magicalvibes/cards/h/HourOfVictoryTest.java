package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.OozePatrol;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HourOfVictory.class, Swamp.class, OozePatrol.class})
class HourOfVictoryTest extends BaseCardTest {

    @Test
    void entersWithZombieAndStartsEngines() {
        harness.setHand(player1, List.of(new HourOfVictory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void maxSpeedAbilitySacrificesAndTutors() {
        Permanent hour = harness.addToBattlefieldAndReturn(player1, new HourOfVictory());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new Swamp(), new OozePatrol()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hour);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals(chosenName));
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        Permanent hour = harness.addToBattlefieldAndReturn(player1, new HourOfVictory());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hour);
    }

    @Test
    void maxSpeedAbilityRequiresSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new HourOfVictory());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void speedThreeIsNotMaxSpeed() {
        Permanent hour = harness.addToBattlefieldAndReturn(player1, new HourOfVictory());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hour);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificesAsCostAndResolvesWithEmptyLibrary() {
        Permanent hour = harness.addToBattlefieldAndReturn(player1, new HourOfVictory());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hour);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hour.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithSpellOnStackDuringOwnMainPhase() {
        Permanent hour = harness.addToBattlefieldAndReturn(player1, new HourOfVictory());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new HourOfVictory()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hour);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void enteringAtMaxSpeedDoesNotResetSpeedAndStillCreatesZombie() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new HourOfVictory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    void unrestrictedSearchMustChooseCardAndCanChooseCreature() {
        harness.addToBattlefield(player1, new HourOfVictory());
        gd.playerSpeeds.put(player1.getId(), 4);
        Swamp land = new Swamp();
        OozePatrol creature = new OozePatrol();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
