package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LibertyPrimeRecharged.class, Spellbook.class, GrizzlyBears.class})
class LibertyPrimeRechargedTest extends BaseCardTest {

    @Test
    @DisplayName("Pays two energy when attacking to keep Liberty Prime")
    void paysEnergyWhenAttacking() {
        Permanent prime = addReadyPrime();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prime);
    }

    @Test
    @DisplayName("Sacrifices itself when attacking without enough energy")
    void sacrificesWhenAttackingWithoutEnoughEnergy() {
        addReadyPrime();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liberty Prime, Recharged");
        harness.assertInGraveyard(player1, "Liberty Prime, Recharged");
    }

    @Test
    @DisplayName("Pays two energy when blocking to keep Liberty Prime")
    void paysEnergyWhenBlocking() {
        Permanent prime = addReadyPrime();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prime);
    }

    @Test
    @DisplayName("Sacrificing an artifact gives two energy and draws a card")
    void sacrificesArtifactForEnergyAndCard() {
        Permanent prime = addReadyPrime();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Card drawnCard = new Spellbook();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawnCard);
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(prime.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack payment sacrifices Liberty Prime without spending energy")
    void declinesEnergyPaymentWhenAttacking() {
        addReadyPrime();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Liberty Prime, Recharged");
        harness.assertInGraveyard(player1, "Liberty Prime, Recharged");
    }

    @Test
    @DisplayName("Blocking with insufficient energy sacrifices Liberty Prime without spending energy")
    void sacrificesWhenBlockingWithoutEnoughEnergy() {
        addReadyPrime();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Liberty Prime, Recharged");
        harness.assertInGraveyard(player1, "Liberty Prime, Recharged");
    }

    @Test
    @DisplayName("Liberty Prime can sacrifice itself and its ability still gives energy and draws")
    void sacrificesItselfForEnergyAndCard() {
        Permanent prime = addReadyPrime();
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Card drawnCard = new LibertyPrimeRecharged();
        harness.setLibrary(player1, List.of(drawnCard));
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, prime.getId());

        harness.assertNotOnBattlefield(player1, "Liberty Prime, Recharged");
        harness.assertInGraveyard(player1, "Liberty Prime, Recharged");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherArtifact);
    }

    private Permanent addReadyPrime() {
        return addCreatureReady(player1, new LibertyPrimeRecharged());
    }
}
