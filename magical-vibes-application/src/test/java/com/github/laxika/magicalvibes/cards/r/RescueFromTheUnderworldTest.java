package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DivineVerdict;
import com.github.laxika.magicalvibes.cards.p.PharikasCure;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.w.WhipOfErebos;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RescueFromTheUnderworld.class, TravelingPhilosopher.class, DivineVerdict.class,
        PharikasCure.class, WhipOfErebos.class})
class RescueFromTheUnderworldTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and the sacrificed creature at the next upkeep")
    void returnsTargetAndSacrificedCreatureAtNextUpkeep() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        RescueFromTheUnderworld rescue = new RescueFromTheUnderworld();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(rescue));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(rescue.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(target.getId(), sacrificed.getCard().getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(target.getId(), sacrificed.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId(), sacrificed.getCard().getId());
    }

    @Test
    @DisplayName("Returns the sacrificed creature if the target card leaves the graveyard")
    void returnsRemainingCardIfTargetLeavesGraveyard() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        RescueFromTheUnderworld rescue = new RescueFromTheUnderworld();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(rescue));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();
        gd.playerGraveyards.get(player1.getId()).remove(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(sacrificed.getCard().getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(target.getId())
                .contains(sacrificed.getCard().getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new DivineVerdict();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, noncreature.getId(), sacrificed.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
    }

    @Test
    @DisplayName("The upkeep return uses the stack and allows responses")
    void returnWaitsForDelayedTriggerToResolve() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(target.getId(), sacrificed.getCard().getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(target.getId(), sacrificed.getCard().getId());
    }

    @Test
    @DisplayName("A creature that leaves and reenters the graveyard is not returned")
    void doesNotReturnNewGraveyardObject() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .addCardToHandFromGraveyard(gd, player1.getId(), player1.getId(), target));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent recast = findPermanent(player1, "Traveling Philosopher");
        harness.setHand(player2, List.of(new PharikasCure()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, recast.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(sacrificed.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("A sacrificed creature exiled by Whip of Erebos still returns")
    void returnsSacrificedCardFromReplacementExile() {
        Card target = new TravelingPhilosopher();
        Card toSacrifice = new TravelingPhilosopher();
        harness.addToBattlefield(player1, new WhipOfErebos());
        harness.setGraveyard(player1, List.of(target, toSacrifice));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, 0, null, toSacrifice.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        Permanent sacrificed = findPermanent(player1, "Traveling Philosopher");
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(toSacrifice.getId())).isNotNull();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(target.getId(), toSacrifice.getId());
        assertThat(gd.findExiledCard(toSacrifice.getId())).isNull();
    }

    @Test
    @DisplayName("An illegal target on resolution prevents both the return and self-exile")
    void illegalTargetPreventsDelayedReturn() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        Card rescue = new RescueFromTheUnderworld();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(rescue));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .addCardToHandFromGraveyard(gd, player1.getId(), player1.getId(), target));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(rescue.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rescue);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Cannot target an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, target.getId(), sacrificed.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
    }

    @Test
    @DisplayName("The opponent's upkeep does not return the creatures")
    void waitsForControllersUpkeep() {
        Card target = new TravelingPhilosopher();
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(target.getId(), sacrificed.getCard().getId());
    }

    @Test
    @DisplayName("Cannot cast without sacrificing a creature")
    void requiresCreatureSacrifice() {
        Card target = new TravelingPhilosopher();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("The sacrificed creature cannot be chosen as the original target")
    void targetIsChosenBeforeSacrifice() {
        Permanent sacrificed = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, sacrificed.getCard().getId(), sacrificed.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
        assertThat(gd.stack).isEmpty();
    }
}
