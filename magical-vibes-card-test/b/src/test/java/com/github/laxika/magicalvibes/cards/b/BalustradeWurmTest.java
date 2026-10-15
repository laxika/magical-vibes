package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalustradeWurm.class, Cancel.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class, Solemnity.class})
class BalustradeWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter when delirium is active")
    void returnsFromGraveyardWithFinalityCounter() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, deliriumGraveyard(wurm));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(wurm);
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Balustrade Wurm");
    }

    @Test
    @DisplayName("Cannot activate without delirium")
    void cannotActivateWithoutDelirium() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, List.of(wurm, new GrizzlyBears(), new Forest(), new Shock()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Balustrade Wurm");
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, deliriumGraveyard(wurm));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A finality counter exiles the returned Wurm instead of putting it into a graveyard")
    void finalityCounterExilesWhenItDies() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, deliriumGraveyard(wurm));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(wurm);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Balustrade Wurm");
        harness.assertNotInGraveyard(player1, "Balustrade Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Balustrade Wurm"));
    }

    @Test
    @DisplayName("This spell can't be countered")
    void cannotBeCountered() {
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, wurm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Balustrade Wurm");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Delirium need not remain active after the ability is activated")
    void returnsAfterDeliriumIsLost() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, deliriumGraveyard(wurm));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of(wurm));
        harness.passBothPriorities();

        assertThat(findPermanent(wurm).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Balustrade Wurm");
    }

    @Test
    @DisplayName("Only the Wurm whose ability was activated returns")
    void returnsOnlySourceWurm() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        BalustradeWurm otherWurm = new BalustradeWurm();
        List<Card> graveyard = deliriumGraveyard(wurm);
        graveyard.add(otherWurm);
        harness.setGraveyard(player1, graveyard);
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(wurm).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherWurm);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(otherWurm.getId()));
    }

    @Test
    @DisplayName("The Wurm itself supplies the creature type for delirium")
    void sourceCountsForDelirium() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, List.of(wurm, new Forest(), new Shock(), new Millstone()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(wurm).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate during the opponent's main phase")
    void cannotActivateOnOpponentsTurn() {
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        harness.setGraveyard(player1, deliriumGraveyard(new BalustradeWurm()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while another spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        prepareMainPhase();
        harness.setGraveyard(player1, deliriumGraveyard(new BalustradeWurm()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        addActivationMana();
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(Solemnity.class)
    @DisplayName("Solemnity prevents the finality counter without preventing the return")
    void solemnityPreventsFinalityCounter() {
        prepareMainPhase();
        harness.addToBattlefield(player2, new Solemnity());
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, deliriumGraveyard(wurm));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(wurm);
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isZero();
        harness.assertNotInGraveyard(player1, "Balustrade Wurm");
    }

    @Test
    @DisplayName("The ability does nothing if its source leaves the graveyard before resolution")
    void doesNotReturnMissingSource() {
        prepareMainPhase();
        BalustradeWurm wurm = new BalustradeWurm();
        harness.setGraveyard(player1, deliriumGraveyard(wurm));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        harness.setExile(player1, List.of(wurm));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balustrade Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wurm);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability requires two green mana, not just four total mana")
    void cannotActivateWithoutEnoughGreenMana() {
        prepareMainPhase();
        harness.setGraveyard(player1, deliriumGraveyard(new BalustradeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Balustrade Wurm");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> deliriumGraveyard(Card source) {
        return new ArrayList<>(List.of(source, new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }
}
