package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.a.AuraBlast;
import com.github.laxika.magicalvibes.cards.s.SeaSnidd;
import com.github.laxika.magicalvibes.cards.s.Singe;
import com.github.laxika.magicalvibes.cards.s.SlingshotGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudCover.class, AncientSpider.class, Singe.class, SlingshotGoblin.class,
        SeaSnidd.class, AuraBlast.class})
class CloudCoverTest extends BaseCardTest {

    @Test
    void opponentTargetingAnotherPermanentOffersBounce() {
        harness.addToBattlefield(player1, new CloudCover());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        UUID spiderId = spider.getId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Singe()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spiderId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(card -> card.getName().equals("Ancient Spider"))).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(spiderId))).isFalse();
    }

    @Test
    void decliningBounceLeavesPermanentOnBattlefield() {
        harness.addToBattlefield(player1, new CloudCover());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        UUID spiderId = spider.getId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Singe()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spiderId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(spiderId))).isTrue();
    }

    @Test
    void ownSpellDoesNotTriggerCloudCover() {
        harness.addToBattlefield(player1, new CloudCover());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        UUID spiderId = spider.getId();

        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, spiderId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentTargetingAnotherNoncreaturePermanentOffersBounce() {
        harness.addToBattlefield(player1, new CloudCover());
        Permanent otherCloudCover = harness.addToBattlefieldAndReturn(player1, new CloudCover());
        UUID otherCloudCoverId = otherCloudCover.getId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AuraBlast()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, otherCloudCoverId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(otherCloudCover.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(otherCloudCoverId))).isFalse();
    }

    @Test
    void ownAbilityDoesNotTriggerCloudCover() {
        addCreatureReady(player1, new SlingshotGoblin());
        harness.addToBattlefield(player1, new CloudCover());
        Permanent seaSnidd = harness.addToBattlefieldAndReturn(player1, new SeaSnidd());
        UUID seaSniddId = seaSnidd.getId();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, seaSniddId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentAbilityTargetingAnotherPermanentOffersBounce() {
        harness.addToBattlefield(player1, new CloudCover());
        Permanent seaSnidd = harness.addToBattlefieldAndReturn(player1, new SeaSnidd());
        UUID seaSniddId = seaSnidd.getId();

        addCreatureReady(player2, new SlingshotGoblin());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, seaSniddId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(card -> card.getName().equals("Sea Snidd"))).isTrue();
    }

    @Test
    void targetingCloudCoverItselfDoesNotTriggerIt() {
        Permanent cloudCover = harness.addToBattlefieldAndReturn(player1, new CloudCover());
        UUID cloudCoverId = cloudCover.getId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AuraBlast()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, cloudCoverId);

        assertThat(gd.stack).hasSize(1);
    }
}
