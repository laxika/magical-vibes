package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilboUnexpectedAdventurer.class, Forest.class, GrizzlyBears.class, HillGiant.class, InvasionOfZendikar.class, Pacifism.class})
class BilboUnexpectedAdventurerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers one nonland permanent card with mana value 3 or less")
    void combatDamageReturnsEligibleCardUnderItsOwnersControl() {
        Card legal = new GrizzlyBears();
        legal.setOwnerId(player2.getId());
        Card tooExpensive = new HillGiant();
        tooExpensive.setOwnerId(player2.getId());
        Card land = new Forest();
        land.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(legal, tooExpensive, land));

        addBilboAttacking();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());

        harness.handleMultipleCardsChosen(player1, List.of(legal.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    @Test
    @DisplayName("The up-to-one graveyard return may be declined")
    void combatDamageMayReturnNothing() {
        Card legal = new GrizzlyBears();
        legal.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(legal));

        addBilboAttacking();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(legal);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    @Test
    @DisplayName("Bilbo cannot be blocked by a creature with power 3 or greater")
    void cannotBeBlockedByPowerThreeCreature() {
        Permanent bilbo = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        bilbo.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bilbo);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    void powerTwoCreatureCanBlockAndCreatureDamageDoesNotTriggerReturn() {
        Card legal = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(legal));
        Permanent bilbo = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        bilbo.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(bilbo))));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(legal);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    @Test
    void removedGraveyardTargetIsNotReturnedOrReplacedByAnotherCard() {
        Card chosen = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen, other));
        addBilboAttacking();

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE,
                () -> harness.handleMultipleCardsChosen(player1, List.of(chosen.getId())));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(chosen));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(chosen.getId())
                        || permanent.getCard().getId().equals(other.getId()));
    }

    @Test
    void combatDamageToBattleReturnsCardFromControllersGraveyard() {
        Card legal = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(legal));
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 4);
        Permanent bilbo = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        bilbo.setAttacking(true);
        bilbo.setAttackTarget(battle.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());
        harness.handleMultipleCardsChosen(player1, List.of(legal.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    void returnedAuraOwnerChoosesWhatItEnchants() {
        Card aura = new Pacifism();
        aura.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(aura));
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addBilboAttacking();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
                    assertThat(permanent.getAttachedTo()).isEqualTo(creature.getId());
                });
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(aura);
    }
    private void addBilboAttacking() {
        Permanent bilbo = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        bilbo.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
