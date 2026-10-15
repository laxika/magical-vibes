package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueDogs.class, PlatedSpider.class})
class PlagueDogsTest extends BaseCardTest {

    @Test
    @DisplayName("When Plague Dogs dies, all creatures get -1/-1 until end of turn")
    void deathTriggerDebuffsAllCreaturesUntilEndOfTurn() {
        Permanent dogs = harness.addToBattlefieldAndReturn(player1, new PlagueDogs());
        harness.addToBattlefield(player1, new PlatedSpider());
        PlatedSpider blockerCard = new PlatedSpider();
        blockerCard.setPower(5);
        blockerCard.setToughness(5);
        Permanent blocker = addCreatureReady(player2, blockerCard);

        dogs.setSummoningSick(false);
        dogs.setAttacking(true);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Plated Spider").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Plated Spider").getEffectiveToughness()).isEqualTo(3);
        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Plated Spider").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Plated Spider").getEffectiveToughness()).isEqualTo(4);
        assertThat(blocker.getEffectivePower()).isEqualTo(5);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing Plague Dogs triggers its death ability and draws a card")
    void sacrificeAbilityTriggersDeathAbilityAndDrawsCard() {
        harness.addToBattlefield(player1, new PlagueDogs());
        harness.addToBattlefield(player1, new PlatedSpider());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Plague Dogs");
        harness.assertInGraveyard(player1, "Plague Dogs");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(findPermanent(player1, "Plated Spider").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Plated Spider").getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the death trigger resolves before the draw")
    void sacrificeIsImmediateAndDeathTriggerResolvesBeforeDraw() {
        Permanent dogs = harness.addToBattlefieldAndReturn(player1, new PlagueDogs());
        dogs.tap();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        harness.setLibrary(player1, List.of(new PlatedSpider()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Plague Dogs");
        harness.assertInGraveyard(player1, "Plague Dogs");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(3);
        assertThat(spider.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Plated Spider");
    }

    @Test
    @DisplayName("The death trigger affects creatures present at resolution but not later arrivals")
    void deathTriggerDeterminesAffectedCreaturesAtResolution() {
        harness.addToBattlefield(player1, new PlagueDogs());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());
        harness.setLibrary(player1, List.of(new PlatedSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player2, new PlatedSpider());
        resolveAllTriggers();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player2, new PlatedSpider());

        assertThat(existing.getEffectivePower()).isEqualTo(3);
        assertThat(existing.getEffectiveToughness()).isEqualTo(3);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(4);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(4);
    }
}
