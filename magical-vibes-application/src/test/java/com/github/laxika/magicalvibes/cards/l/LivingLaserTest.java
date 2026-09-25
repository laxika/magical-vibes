package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.ZombieInfestation;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingLaser.class, ZombieInfestation.class, AvenFisher.class, Mountain.class})
class LivingLaserTest extends BaseCardTest {

    @Test
    void createsOneNonlegendaryTappedAttackingCopyPerCardDiscardedThisTurn() {
        discardTwoCards();
        Permanent livingLaser = addCreatureReady(player1, new LivingLaser());
        harness.addToBattlefield(player2, new AvenFisher());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(livingLaser)));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Living Laser").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    void createdCopiesAreExiledAtTheBeginningOfTheNextEndStep() {
        discardTwoCards();
        Permanent livingLaser = addCreatureReady(player1, new LivingLaser());
        harness.addToBattlefield(player2, new AvenFisher());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(livingLaser)));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Living Laser")).hasSize(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Living Laser")).containsExactly(livingLaser);
    }

    @Test
    void createsNoCopiesWhenNoCardsHaveBeenDiscardedThisTurn() {
        Permanent livingLaser = addCreatureReady(player1, new LivingLaser());
        harness.addToBattlefield(player2, new AvenFisher());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(livingLaser)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Living Laser")).containsExactly(livingLaser);
    }

    private void discardTwoCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new AvenFisher(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
