package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CaseOfTheBurningMasks.class, GiantSpider.class, LightningStrike.class, Shock.class})
class CaseOfTheBurningMasksTest extends BaseCardTest {

    @Test
    @DisplayName("Solves after three different sources deal damage this turn")
    void solvesAfterThreeDifferentSourcesDealDamage() {
        Permanent casePermanent = addCaseAfterItsDamageResolves();
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castLightningStrike(secondTarget);
        castLightningStrike(thirdTarget);
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("The solved ability sacrifices the Case and offers one of the top three cards to play")
    void solvedAbilityExilesThreeAndOffersOneCard() {
        addCaseAfterItsDamageResolves();
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        castLightningStrike(secondTarget);
        castLightningStrike(thirdTarget);
        resolveEndStepTriggers();

        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Case of the Burning Masks")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
    }

    private Permanent addCaseAfterItsDamageResolves() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CaseOfTheBurningMasks()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Case of the Burning Masks");
    }

    private void castLightningStrike(Permanent target) {
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
