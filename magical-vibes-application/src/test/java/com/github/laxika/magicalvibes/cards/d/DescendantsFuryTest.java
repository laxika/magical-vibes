package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendantsFury.class, Forest.class, GrizzlyBears.class, ShivanDragon.class})
class DescendantsFuryTest extends BaseCardTest {

    @Test
    void maySacrificeOneOfTheCombatDealersAndPutAMatchingCreatureOntoTheBattlefield() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent dragon = addReadyCreature(new ShivanDragon());
        bears.setAttacking(true);
        dragon.setAttacking(true);

        Card nonmatching = new Forest();
        Card matching = new ShivanDragon();
        harness.setLibrary(player1, List.of(nonmatching, matching));

        resolveCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dragon);
        assertThat(findPermanents(player1, "Shivan Dragon")).hasSize(1);
        harness.assertInGraveyard(player1, "Shivan Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    void mayDeclineWithoutSacrificingOrRevealing() {
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        bears.setAttacking(true);
        Card topCard = new ShivanDragon();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatDamage();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addReadyCreature(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
