package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.d.DaringBuccaneer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FranciscoFowlMarauder.class, DaringBuccaneer.class, Forest.class,
        GrizzlyBears.class, HermeticStudy.class})
class FranciscoFowlMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("A Pirate dealing combat damage makes Francisco explore")
    void pirateCombatDamageMakesFranciscoExplore() {
        Permanent francisco = harness.addToBattlefieldAndReturn(player1, new FranciscoFowlMarauder());
        addAttacker(new DaringBuccaneer());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        resolveDamageAndTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(francisco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Francisco's trigger batches multiple Pirates dealing damage")
    void batchesMultiplePiratesDealingDamage() {
        Permanent francisco = harness.addToBattlefieldAndReturn(player1, new FranciscoFowlMarauder());
        addAttacker(new DaringBuccaneer());
        addAttacker(new DaringBuccaneer());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        resolveDamageAndTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(francisco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A Pirate dealing noncombat damage makes Francisco explore")
    void pirateNoncombatDamageMakesFranciscoExplore() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new DaringBuccaneer());
        pirate.setSummoningSick(false);
        harness.addToBattlefield(player1, new FranciscoFowlMarauder());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(pirate.getId());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("A non-Pirate dealing damage does not make Francisco explore")
    void nonPirateDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new FranciscoFowlMarauder());
        addAttacker(new GrizzlyBears());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        resolveDamageAndTriggers();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
    }

    @Test
    @DisplayName("Francisco cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        harness.addToBattlefield(player2, new FranciscoFowlMarauder());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private void resolveDamageAndTriggers() {
        resolveCombat();
        resolveAllTriggers();
    }

    @Test
    void nonlandCanBePutIntoGraveyard() {
        Permanent francisco = harness.addToBattlefieldAndReturn(player1, new FranciscoFowlMarauder());
        addAttacker(new DaringBuccaneer());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));

        resolveDamageAndTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(francisco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryStillGivesCounter() {
        Permanent francisco = harness.addToBattlefieldAndReturn(player1, new FranciscoFowlMarauder());
        addAttacker(new DaringBuccaneer());
        harness.setLibrary(player1, List.of());

        resolveDamageAndTriggers();

        assertThat(francisco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void damageToControllerAlsoTriggersExplore() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new DaringBuccaneer());
        pirate.setSummoningSick(false);
        harness.addToBattlefield(player1, new FranciscoFowlMarauder());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(pirate.getId());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void franciscoStillExploresAfterLeavingBattlefield() {
        Permanent francisco = harness.addToBattlefieldAndReturn(player1, new FranciscoFowlMarauder());
        addAttacker(new DaringBuccaneer());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        gd.playerBattlefields.get(player1.getId()).remove(francisco);
        gd.playerGraveyards.get(player1.getId()).add(francisco.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(francisco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
