package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzoriusHerald;
import com.github.laxika.magicalvibes.cards.c.Condemn;
import com.github.laxika.magicalvibes.cards.e.EntropicEidolon;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainOfGore.class, Condemn.class, AzoriusHerald.class, EntropicEidolon.class,
        VampireNighthawk.class, PreyUpon.class, RhoxFaithmender.class})
class RainOfGoreTest extends BaseCardTest {

    @Test
    @DisplayName("A spell causing its controller to gain life causes life loss instead")
    void spellControllerLifeGainBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        Permanent attacker = addCreatureReady(player1, new AzoriusHerald());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
        harness.setHand(player1, List.of(new Condemn()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A spell causing another player to gain life is unaffected")
    void anotherPlayersLifeGainIsUnaffected() {
        harness.addToBattlefield(player1, new RainOfGore());
        Permanent attacker = addCreatureReady(player2, new AzoriusHerald());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player2, List.of(0)));
        harness.setHand(player1, List.of(new Condemn()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("An ability causing its controller to gain life causes life loss instead")
    void abilityControllerLifeGainBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.addToBattlefield(player1, new EntropicEidolon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Combat damage lifelink is unaffected")
    void combatDamageLifelinkIsUnaffected() {
        addCreatureReady(player1, new VampireNighthawk());
        harness.addToBattlefield(player2, new RainOfGore());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Triggered life gain becomes life loss even if its source is sacrificed")
    void triggeredLifeGainBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.setHand(player1, List.of(new AzoriusHerald()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertInGraveyard(player1, "Azorius Herald");
    }

    @Test
    @DisplayName("Multiple Rains of Gore replace a gain only once")
    void multipleCopiesDoNotMultiplyLifeLoss() {
        harness.addToBattlefield(player1, new RainOfGore());
        harness.addToBattlefield(player2, new RainOfGore());
        harness.addToBattlefield(player1, new EntropicEidolon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Lifelink from a fight caused by its controller's spell becomes life loss")
    void ownFightSpellLifelinkBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        Permanent lifelinker = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AzoriusHerald());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(lifelinker.getId(), opponent.getId()));

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lifelink from a fight caused by an opponent's spell remains life gain")
    void opposingFightSpellLifelinkIsUnaffected() {
        harness.addToBattlefield(player2, new RainOfGore());
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new AzoriusHerald());
        Permanent lifelinker = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(fighter.getId(), lifelinker.getId()));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("The affected player chooses between life-gain doubling and Rain of Gore")
    void playerChoosesLifeGainReplacementOrder() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.addToBattlefield(player1, new EntropicEidolon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
