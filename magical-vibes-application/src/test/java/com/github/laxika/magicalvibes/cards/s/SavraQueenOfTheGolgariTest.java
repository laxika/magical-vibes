package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CarrionHowler;
import com.github.laxika.magicalvibes.cards.c.CullingDais;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariRotwurm;
import com.github.laxika.magicalvibes.cards.g.GreaterMossdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavraQueenOfTheGolgari.class, CullingDais.class, CarrionHowler.class,
        GreaterMossdog.class, GlassGolem.class, GolgariRotwurm.class})
class SavraQueenOfTheGolgariTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a black creature and paying 2 life makes each opponent sacrifice a creature")
    void blackCreatureSacrificeTriggersOpponentSacrifice() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GreaterMossdog());
        Permanent sacrificed = addSavraAndDais(player1, new CarrionHowler());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Carrion Howler");
        harness.assertNotOnBattlefield(player2, opponentCreature.getCard().getName());
        harness.assertInGraveyard(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Declining Savra's black-creature payment leaves opponents' creatures alone")
    void decliningBlackCreaturePaymentDoesNothing() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GreaterMossdog());
        Permanent sacrificed = addSavraAndDais(player1, new CarrionHowler());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Sacrificing a green creature and accepting gains 2 life")
    void greenCreatureSacrificeGainsLife() {
        harness.setLife(player1, 20);
        Permanent sacrificed = addSavraAndDais(player1, new GreaterMossdog());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Greater Mossdog");
    }

    @Test
    @DisplayName("Declining Savra's green-creature life gain leaves life unchanged")
    void decliningGreenCreatureLifeGainDoesNothing() {
        harness.setLife(player1, 20);
        Permanent sacrificed = addSavraAndDais(player1, new GreaterMossdog());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Greater Mossdog");
    }

    @Test
    @DisplayName("Sacrificing a colorless creature does not trigger Savra")
    void colorlessCreatureSacrificeDoesNotTrigger() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GreaterMossdog());
        Permanent sacrificed = addSavraAndDais(player1, new GlassGolem());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Glass Golem");
        harness.assertOnBattlefield(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Each opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.setLife(player1, 20);
        Permanent remainingOpponentCreature = addCreature(player2, new GreaterMossdog());
        Permanent sacrificedOpponentCreature = addCreature(player2, new CarrionHowler());
        Permanent sacrificed = addSavraAndDais(player1, new CarrionHowler());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player2, sacrificedOpponentCreature.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Carrion Howler");
        harness.assertInGraveyard(player2, "Carrion Howler");
        harness.assertOnBattlefield(player2, remainingOpponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Sacrificing a black-green creature triggers both of Savra's abilities")
    void blackGreenCreatureSacrificeTriggersBothAbilities() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GreaterMossdog());
        Permanent sacrificed = addSavraAndDais(player1, new GolgariRotwurm());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Golgari Rotwurm");
        harness.assertInGraveyard(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Sacrificing Savra herself triggers both abilities")
    void sacrificingSavraTriggersBothAbilities() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GreaterMossdog());
        harness.addToBattlefield(player1, new SavraQueenOfTheGolgari());
        harness.addToBattlefield(player1, new CullingDais());

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Savra, Queen of the Golgari");
        harness.assertInGraveyard(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Paying for a black sacrifice is allowed when the opponent has no creatures")
    void blackPaymentWithNoOpponentCreatures() {
        harness.setLife(player1, 20);
        Permanent sacrificed = addSavraAndDais(player1, new CarrionHowler());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Savra, Queen of the Golgari");
        harness.assertInGraveyard(player1, "Carrion Howler");
    }

    @Test
    @DisplayName("Savra cannot make an opponent sacrifice when her controller cannot pay 2 life")
    void insufficientLifeDoesNotCauseOpponentSacrifice() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player2, new GreaterMossdog());
        Permanent sacrificed = addSavraAndDais(player1, new CarrionHowler());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Greater Mossdog");
        harness.assertInGraveyard(player1, "Carrion Howler");
    }

    @Test
    @DisplayName("An opponent sacrificing a black-green creature does not trigger Savra")
    void opponentSacrificeDoesNotTriggerSavra() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SavraQueenOfTheGolgari());
        harness.addToBattlefield(player2, new CullingDais());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, sacrificed.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Savra, Queen of the Golgari");
        harness.assertInGraveyard(player2, "Golgari Rotwurm");
    }
    private Permanent addSavraAndDais(Player player, Card sacrificedCard) {
        harness.addToBattlefield(player, new SavraQueenOfTheGolgari());
        harness.addToBattlefield(player, new CullingDais());
        return harness.addToBattlefieldAndReturn(player, sacrificedCard);
    }

    private Permanent addCreature(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
