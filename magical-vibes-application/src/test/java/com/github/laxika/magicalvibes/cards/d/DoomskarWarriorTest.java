package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({DoomskarWarrior.class, Forest.class, GrizzlyBears.class, Shock.class})
class DoomskarWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants another creature a combat-damage library ability")
    void backupGrantsCombatDamageAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        Card nonmatching = new Shock();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatching, land, creature));

        castWarriorTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        declareAttackersAndResolveCombat(0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(nonmatching, land, creature);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonmatching, creature);
    }

    @Test
    @DisplayName("The source's combat-damage ability uses the damage amount")
    void sourceCombatDamageAbilityUsesDamageAmount() {
        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, land, creature));

        Permanent warrior = castWarriorTargetingItself();
        warrior.setSummoningSick(false);
        declareAttackersAndResolveCombat(0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(first, second, third, land, creature);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), creature.getId());
    }

    private void castWarriorTargeting(Permanent target) {
        harness.setHand(player1, List.of(new DoomskarWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Permanent castWarriorTargetingItself() {
        harness.setHand(player1, List.of(new DoomskarWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent warrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DoomskarWarrior)
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();
        return warrior;
    }

    private void declareAttackersAndResolveCombat(int attackerIndex) {
        declareAttackers(List.of(attackerIndex));
        resolveCombat();
        harness.passBothPriorities();
    }
}
