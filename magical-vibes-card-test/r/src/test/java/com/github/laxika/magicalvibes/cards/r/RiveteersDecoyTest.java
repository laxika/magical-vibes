package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.c.ConspiracyUnraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({RiveteersDecoy.class, CivicGardener.class, Murder.class, Humility.class, ConspiracyUnraveler.class,
        GrizzlyBears.class, Stifle.class})
class RiveteersDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent decoy = addCreatureReady(player1, new RiveteersDecoy());
        decoy.setAttacking(true);
        addCreatureReady(player2, new CivicGardener());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent decoy = findPermanent(player1, "Riveteers Decoy");
        assertThat(gqs.hasKeyword(gd, decoy, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Riveteers Decoy");
        harness.assertInHand(player1, "Civic Gardener");
    }

    @Test
    @DisplayName("Under an earlier Humility a blitzed creature has no haste and draws nothing when it dies")
    void earlierHumilityRemovesBlitzAbilities() {
        harness.addToBattlefield(player1, new Humility());
        Permanent decoy = blitzDecoy();

        assertThat(gqs.hasKeyword(gd, decoy, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, decoy, player1.getId())).isFalse();

        murder(decoy);

        harness.assertInGraveyard(player1, "Riveteers Decoy");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cast for a different alternative cost: no haste, no end-step sacrifice and no draw on death")
    void otherAlternativeCostIsNotBlitz() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.setHand(player2, List.of());
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        assertThat(gd.stack.getLast().isAlternateCost()).isTrue();
        harness.passBothPriorities();
        Permanent decoy = findPermanent(player1, "Riveteers Decoy");

        assertThat(gqs.hasKeyword(gd, decoy, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, decoy, player1.getId())).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Riveteers Decoy")).isSameAs(decoy);

        murder(decoy);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A blitzed creature whose sacrifice is countered keeps haste past this turn")
    void stifledSacrificeKeepsHaste() {
        harness.forceActivePlayer(player1);
        Permanent decoy = blitzDecoy();
        harness.setHand(player1, List.of(new Stifle()));
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanent(player1, "Riveteers Decoy")).isSameAs(decoy);
        assertThat(gqs.hasKeyword(gd, decoy, Keyword.HASTE)).isTrue();
    }

    private Permanent blitzDecoy() {
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Riveteers Decoy");
    }

    private void murder(Permanent target) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }

    @Test
    void blitzHasHasteImmediatelyWhenSpellResolves() {
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Riveteers Decoy"), Keyword.HASTE)).isTrue();
    }

    @Test
    void normalCastDoesNotGainHasteOrSacrificeAtEndStep() {
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Riveteers Decoy"), Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Riveteers Decoy");
    }

    @Test
    void blitzDrawsWhenDestroyedBeforeEndStep() {
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Riveteers Decoy").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Riveteers Decoy");
        harness.assertInHand(player1, "Civic Gardener");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void normalCastDoesNotDrawWhenDestroyed() {
        harness.setHand(player1, List.of(new RiveteersDecoy()));
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Riveteers Decoy").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Riveteers Decoy");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void onlyOneBlockerIsRequired() {
        addCreatureReady(player1, new RiveteersDecoy()).setAttacking(true);
        addCreatureReady(player2, new CivicGardener());
        addCreatureReady(player2, new CivicGardener());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }
}
