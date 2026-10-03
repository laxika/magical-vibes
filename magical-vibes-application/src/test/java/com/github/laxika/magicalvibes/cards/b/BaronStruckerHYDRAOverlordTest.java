package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AgentsOfHYDRA;
import com.github.laxika.magicalvibes.cards.a.AgentsOfSHIELD;
import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaronStruckerHYDRAOverlord.class, DocOcksHenchmen.class, GrizzlyBears.class, Mountain.class,
        AgentsOfHYDRA.class, AgentsOfSHIELD.class})
class BaronStruckerHYDRAOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the cost of Villain spells")
    void reducesVillainSpellCost() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce the cost of non-Villain spells")
    void doesNotReduceNonVillainSpellCost() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May connive the entering Villain and counters that creature")
    void mayConniveEnteringVillain() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        DocOcksHenchmen villain = new DocOcksHenchmen();
        harness.setHand(player1, List.of(villain, new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        Permanent entering = permanentFor(villain);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("A declined connive may trigger again, but acceptance consumes the turn")
    void conniveIsLimitedOnAcceptance() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        DocOcksHenchmen firstVillain = new DocOcksHenchmen();
        DocOcksHenchmen secondVillain = new DocOcksHenchmen();
        harness.setHand(player1, List.of(firstVillain, secondVillain, new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(permanentFor(firstVillain).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(permanentFor(secondVillain).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger again after conniving once")
    void doesNotTriggerAfterConnivingOnce() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        DocOcksHenchmen firstVillain = new DocOcksHenchmen();
        DocOcksHenchmen secondVillain = new DocOcksHenchmen();
        harness.setHand(player1, List.of(firstVillain, secondVillain, new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(permanentFor(secondVillain).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Discarding a land gives no counter and still consumes the connive for this turn")
    void landDiscardConsumesConnive() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new AgentsOfSHIELD()));
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new AgentsOfHYDRA());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Agents of S.H.I.E.L.D.");
        harness.assertInGraveyard(player1, "Mountain");
        harness.enterBattlefieldAndReturn(player1, new AgentsOfHYDRA());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Connive becomes available again on the opponent's turn")
    void conniveResetsOnOpponentsTurn() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new AgentsOfSHIELD(), new AgentsOfSHIELD()));
        Permanent first = harness.enterBattlefieldAndReturn(player1, new AgentsOfHYDRA());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        Permanent second = harness.enterBattlefieldAndReturn(player1, new AgentsOfHYDRA());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Baron does not connive itself when it enters")
    void doesNotTriggerForItself() {
        harness.enterBattlefieldAndReturn(player1, new BaronStruckerHYDRAOverlord());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Non-Villains and opposing Villains do not trigger connive")
    void ignoresNonVillainsAndOpposingVillains() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        harness.enterBattlefieldAndReturn(player1, new AgentsOfSHIELD());
        harness.enterBattlefieldAndReturn(player2, new AgentsOfHYDRA());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Villain cost reduction does not pay colored mana or apply to opponents")
    void reductionPreservesColoredCostsAndControllerScope() {
        addCreatureReady(player1, new BaronStruckerHYDRAOverlord());
        harness.setHand(player1, List.of(new AgentsOfHYDRA()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AgentsOfHYDRA()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
