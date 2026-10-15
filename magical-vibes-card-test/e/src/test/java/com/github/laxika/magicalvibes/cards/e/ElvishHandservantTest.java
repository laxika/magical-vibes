package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.b.BladesOfVelisVel;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishHandservant.class, BlindSpotGiant.class,
        WoodlandChangeling.class, BladesOfVelisVel.class})
class ElvishHandservantTest extends BaseCardTest {

    private void giveGiantSpell(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new BlindSpotGiant()));
        harness.addMana(caster, ManaColor.RED, 5);
    }

    @Test
    @DisplayName("Casting a Giant spell offers the controller a may ability")
    void giantSpellTriggers() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting puts a +1/+1 counter on Elvish Handservant")
    void acceptAddsCounter() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = findPermanent(player1, "Elvish Handservant");
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, handservant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, handservant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining leaves Elvish Handservant without a counter")
    void declineLeavesNoCounter() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = findPermanent(player1, "Elvish Handservant");
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a non-Giant spell does not trigger the ability")
    void nonGiantDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        harness.setHand(player1, List.of(new ElvishHandservant()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player casting a Giant spell triggers the controller's ability")
    void opponentGiantTriggersController() {
        harness.addToBattlefield(player1, new ElvishHandservant());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveGiantSpell(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A changeling creature spell adds exactly one counter")
    void changelingCreatureTriggersOnce() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = findPermanent(player1, "Elvish Handservant");
        harness.setHand(player1, List.of(new WoodlandChangeling()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Woodland Changeling")).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature changeling spell is also a Giant spell")
    void kindredInstantTriggers() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = findPermanent(player1, "Elvish Handservant");
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter is placed before the triggering Giant spell resolves")
    void counterResolvesBeforeGiantEnters() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = findPermanent(player1, "Elvish Handservant");
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Blind-Spot Giant")).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blind-Spot Giant")).isEqualTo(1);
        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
