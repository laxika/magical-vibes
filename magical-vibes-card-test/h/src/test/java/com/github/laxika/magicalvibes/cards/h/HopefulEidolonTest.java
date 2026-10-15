package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HopefulEidolon.class, TravelingPhilosopher.class, LightningStrike.class})
class HopefulEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Hopeful Eidolon deals combat damage and gains its controller that much life")
    void creatureHasLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HopefulEidolon());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and grants it lifelink")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HopefulEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Hopeful Eidolon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HopefulEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = findPermanent(player1, "Hopeful Eidolon");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    void normalCastingNeedsNoTargetAndDoesNotBoostItself() {
        harness.setHand(player1, List.of(new HopefulEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = findPermanent(player1, "Hopeful Eidolon");
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        eidolon.setSummoningSick(false);
        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void bestowResolvesAsCreatureWhenTargetDiesBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HopefulEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertNotInGraveyard(player1, "Hopeful Eidolon");
        Permanent eidolon = findPermanent(player1, "Hopeful Eidolon");
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
        assertThat(gqs.hasKeyword(gd, eidolon, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void bestowedLifelinkGainsLifeForEnchantedCreaturesController() {
        Permanent host = addCreatureReady(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HopefulEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent eidolon = findPermanent(player1, "Hopeful Eidolon");
        assertThat(eidolon.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, eidolon)).isFalse();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }
}
