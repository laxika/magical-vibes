package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Takklemaggot.class, BarbaryApes.class, ChainLightning.class})
class TakklemaggotTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -0/-1 counter on the enchanted creature at its controller's upkeep")
    void putsCounterAtEnchantedCreatureControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new BarbaryApes());
        attachTakklemaggot(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE))
                .isZero();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The enchanted creature's controller chooses among all legal creatures")
    void controllerChoosesAmongAllLegalCreatures() {
        Permanent dyingCreature = addCreatureReady(player2, new BarbaryApes());
        Permanent firstTarget = addCreatureReady(player2, new BarbaryApes());
        Permanent chosenTarget = addCreatureReady(player1, new BarbaryApes());
        attachTakklemaggot(player1, dyingCreature);

        destroyCreature(dyingCreature);
        harness.handlePermanentChosen(player2, chosenTarget.getId());

        Permanent returned = findPermanent(player1, "Takklemaggot");
        assertThat(returned.getAttachedTo()).isEqualTo(chosenTarget.getId());
        assertThat(returned.getAttachedTo()).isNotEqualTo(firstTarget.getId());
        harness.assertNotInGraveyard(player1, "Takklemaggot");
    }

    @Test
    @DisplayName("Returns as a non-Aura enchantment and damages the dead creature's controller")
    void returnsAsNonAuraAndDamagesDyingCreatureController() {
        Permanent dyingCreature = addCreatureReady(player2, new BarbaryApes());
        attachTakklemaggot(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent returned = findPermanent(player1, "Takklemaggot");
        assertThat(returned.getAttachedTo()).isNull();
        assertThat(returned.getCard().getSubtypes()).doesNotContain(CardSubtype.AURA);
        assertThat(returned.getCard().getTargetFilter()).isNull();

        int player2LifeBeforeUpkeep = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, player2LifeBeforeUpkeep - 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, player2LifeBeforeUpkeep - 1);
    }

    private Permanent attachTakklemaggot(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new Takklemaggot());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ChainLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
    }
}
