package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendantsFury.class, GrizzlyBears.class, RuneclawBear.class, Shock.class, AssaultSuit.class})
class DescendantsFuryTest extends BaseCardTest {

    @Test
    void mayDeclineSacrifice() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new RuneclawBear()));

        dealUnblockedCombat(attacker);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof RuneclawBear);
    }

    @Test
    void sacrificesChosenDealerAndRevealsUntilSharedCreatureType() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent firstAttacker = addReadyCreature(new GrizzlyBears());
        Permanent secondAttacker = addReadyCreature(new GrizzlyBears());
        Card nonmatching = new Shock();
        Card matching = new RuneclawBear();
        harness.setLibrary(player1, List.of(nonmatching, matching));

        dealUnblockedCombat(firstAttacker, secondAttacker);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstAttacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstAttacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondAttacker);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == matching);
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonmatching);
    }

    @Test
    void cannotSacrificeDamageDealerProtectedByAssaultSuit() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(attacker.getId());
        Card matching = new RuneclawBear();
        harness.setLibrary(player1, List.of(matching));

        dealUnblockedCombat(attacker);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matching);
    }

    @Test
    void singleDamageDealerIsSacrificedAndUnrevealedCardsStayAboveRest() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        Card revealed = new Shock();
        Card matching = new RuneclawBear();
        Card unrevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed, matching, unrevealed));

        dealUnblockedCombat(attacker);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, revealed);
    }

    @Test
    void noMatchingCreatureReturnsEntireLibraryWithoutPuttingAnythingOntoBattlefield() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        dealUnblockedCombat(attacker);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void emptyLibraryStillAllowsSacrifice() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        dealUnblockedCombat(attacker);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
    private Permanent addReadyCreature(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void dealUnblockedCombat(Permanent... attackers) {
        for (Permanent attacker : attackers) {
            attacker.setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.COMBAT_DAMAGE);
        harness.passBothPriorities();
    }
}
