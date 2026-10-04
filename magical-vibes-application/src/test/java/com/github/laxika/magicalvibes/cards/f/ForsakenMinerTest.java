package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.DesperateBloodseeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForsakenMiner.class, Shock.class, DesperateBloodseeker.class})
class ForsakenMinerTest extends BaseCardTest {

    @Test
    @DisplayName("Pays black mana to return from the graveyard after a crime")
    void paysToReturnFromGraveyard() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(miner.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(miner.getId()));
    }

    @Test
    @DisplayName("Declining the payment leaves it in the graveyard")
    void decliningPaymentLeavesMinerInGraveyard() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(miner.getId()));
    }

    @Test
    @DisplayName("Targeting yourself does not trigger the graveyard ability")
    void targetingYourselfDoesNotTriggerAbility() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Forsaken Miner cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ForsakenMiner());
        Permanent blocker = addCreatureReady(player2, new ForsakenMiner());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("An opponent's crime does not return your Miner")
    void opponentsCrimeDoesNotTrigger() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        harness.assertNotOnBattlefield(player1, "Forsaken Miner");
    }

    @Test
    @DisplayName("Targeting an opponent's creature returns Miner before the crime spell resolves")
    void targetingOpponentsPermanentTriggersBeforeSpellResolves() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForsakenMiner());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forsaken Miner");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(miner);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(findPermanent(player1, "Forsaken Miner").isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Dying to the crime spell does not trigger an ability from the battlefield")
    void dyingToCrimeSpellDoesNotTrigger() {
        ForsakenMiner miner = new ForsakenMiner();
        Permanent target = harness.addToBattlefieldAndReturn(player2, miner);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(miner);
        harness.assertNotOnBattlefield(player2, "Forsaken Miner");
    }

    @Test
    @DisplayName("A targeted enters ability is a crime and triggers the graveyard ability")
    void targetedTriggeredAbilityIsCrime() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setLibrary(player2, List.of(new ForsakenMiner(), new ForsakenMiner(), new ForsakenMiner()));
        harness.setHand(player1, List.of(new DesperateBloodseeker()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forsaken Miner");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(miner);
    }

    @Test
    @DisplayName("An older crime trigger cannot return a Miner that has left and reentered the graveyard")
    void olderTriggerCannotReturnNewGraveyardObject() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent returnedMiner = findPermanent(player1, "Forsaken Miner");

        harness.castAndResolveInstant(player1, 0, returnedMiner.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        harness.assertNotOnBattlefield(player1, "Forsaken Miner");
    }
}
