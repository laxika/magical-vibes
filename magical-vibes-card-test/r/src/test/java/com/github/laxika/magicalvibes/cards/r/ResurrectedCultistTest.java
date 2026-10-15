package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ResurrectedCultist.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class, Reclaim.class})
class ResurrectedCultistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter when delirium is active")
    void returnsFromGraveyardWithFinalityCounter() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Resurrected Cultist");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Cannot activate without delirium")
    void cannotActivateWithoutDelirium() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, List.of(cultist, new GrizzlyBears(), new Forest(), new Shock()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Finality counter exiles the returned Cultist instead of putting it into a graveyard")
    void finalityCounterExilesWhenItDies() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Resurrected Cultist");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Resurrected Cultist");
        harness.assertNotInGraveyard(player1, "Resurrected Cultist");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Resurrected Cultist"));
    }

    @Test
    @DisplayName("Only the activated copy returns from the graveyard")
    void returnsOnlyActivatedCopy() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        ResurrectedCultist otherCultist = new ResurrectedCultist();
        List<Card> graveyard = deliriumGraveyard(cultist);
        graveyard.add(otherCultist);
        harness.setGraveyard(player1, graveyard);
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Resurrected Cultist")).hasSize(1);
        assertThat(findPermanent(player1, "Resurrected Cultist").getCard().getId()).isEqualTo(cultist.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCultist).doesNotContain(cultist);
    }

    @Test
    @DisplayName("Losing delirium after activation does not stop the return")
    void returnsAfterDeliriumIsLost() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card instanceof Millstone);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Resurrected Cultist").getCounterCount(CounterType.FINALITY))
                .isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Resurrected Cultist")).hasSize(1);
    }

    @Test
    @DisplayName("Does not return if the source leaves the graveyard before resolution")
    void doesNotReturnWhenSourceLeavesGraveyard() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);

        gd.playerGraveyards.get(player1.getId()).remove(cultist);
        gd.getPlayerExiledCards(player1.getId()).add(cultist);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Resurrected Cultist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cultist);
    }

    @Test
    @DisplayName("Cannot activate during the opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Cannot pay the activation cost with only one black mana")
    void cannotActivateWithoutTwoBlackMana() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Resurrected Cultist");
        harness.assertNotOnBattlefield(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Removing the finality counter allows the returned Cultist to die normally")
    void diesNormallyAfterFinalityCounterIsRemoved() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Resurrected Cultist");
        returned.setCounterCount(CounterType.FINALITY, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Resurrected Cultist");
        harness.assertInGraveyard(player1, "Resurrected Cultist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(cultist);
    }

    @Test
    @DisplayName("An old activation cannot return the Cultist after it leaves and reenters the graveyard")
    void doesNotReturnSourceAfterLeavingAndReenteringGraveyard() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addToBattlefield(player1, new Millstone());
        addActivationMana();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.castAndResolveInstant(player1, 0, cultist.getId());
        harness.assertNotInGraveyard(player1, "Resurrected Cultist");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isEqualTo(cultist);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Resurrected Cultist");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Resurrected Cultist");
        harness.assertInGraveyard(player1, "Resurrected Cultist");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> deliriumGraveyard(Card source) {
        return new ArrayList<>(List.of(source, new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }
}
