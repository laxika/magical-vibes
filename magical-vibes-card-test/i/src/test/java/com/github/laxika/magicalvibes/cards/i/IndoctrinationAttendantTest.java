package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndoctrinationAttendant.class, CrawlingChorus.class, Plains.class})
class IndoctrinationAttendantTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new IndoctrinationAttendant()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returning another permanent creates a toxic Mite")
    void returningAnotherPermanentCreatesMite() {
        Permanent chorus = addCreatureReady(player1, new CrawlingChorus());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(chorus.getId());
        harness.handlePermanentChosen(player1, chorus.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Crawling Chorus");
        Permanent mite = findPermanent(player1, "Mite");
        assertThat(mite.getCard().getKeywords()).contains(Keyword.TOXIC);
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("Declining the return leaves the battlefield unchanged")
    void decliningReturnDoesNothing() {
        addCreatureReady(player1, new CrawlingChorus());
        castAndResolve();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Crawling Chorus");
        assertThat(findPermanents(player1, "Mite")).isEmpty();
    }

    @Test
    @DisplayName("No Mite is created when no other permanent can be returned")
    void noOtherPermanentMeansNoMite() {
        castAndResolve();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Indoctrination Attendant");
        assertThat(findPermanents(player1, "Mite")).isEmpty();
    }

    @Test
    void attendantGivesPoisonAlongsideCombatDamage() {
        Permanent attendant = addCreatureReady(player1, new IndoctrinationAttendant());
        attendant.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void miteGivesPoisonAlongsideCombatDamageWithoutATrigger() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        resolveAllTriggers();
        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returningLandCreatesMiteWithinTheSameResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());

        harness.handlePermanentChosen(player1, land.getId());

        harness.assertInHand(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(findPermanents(player1, "Mite")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
