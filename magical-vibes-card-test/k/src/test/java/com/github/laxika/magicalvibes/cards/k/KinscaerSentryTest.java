package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronShieldElf;
import com.github.laxika.magicalvibes.cards.l.Luminollusk;
import com.github.laxika.magicalvibes.cards.o.OkoLorwynLiege;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinscaerSentry.class, AirElemental.class, GrizzlyBears.class, KnightOfMeadowgrain.class,
        IronShieldElf.class, OkoLorwynLiege.class, Luminollusk.class})
class KinscaerSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature with mana value up to the attacker count onto the battlefield tapped and attacking")
    void putsCreatureWithManaValueUpToAttackerCountTappedAndAttacking() {
        addCreatureReady(player1, new KinscaerSentry());
        addCreatureReady(player1, new KnightOfMeadowgrain());
        GrizzlyBears bears = new GrizzlyBears();
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(bears, elemental));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered).isNotNull();
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.isAttackedThisTurn()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
    }

    @Test
    @DisplayName("Does not offer creatures above the number of attacking creatures")
    void doesNotOfferCreatureAboveAttackerCount() {
        addCreatureReady(player1, new KinscaerSentry());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineToPutAnEligibleCreatureOntoTheBattlefield() {
        addCreatureReady(player1, new KinscaerSentry());
        addCreatureReady(player1, new IronShieldElf());
        KinscaerSentry card = new KinscaerSentry();
        harness.setHand(player1, List.of(card));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(countPermanents(player1, "Kinscaer Sentry")).isEqualTo(1);
    }

    @Test
    void usesTheRemainingAttackerCountWhenResolving() {
        addCreatureReady(player1, new KinscaerSentry());
        Permanent elf = addCreatureReady(player1, new IronShieldElf());
        KinscaerSentry card = new KinscaerSentry();
        harness.setHand(player1, List.of(card));

        declareAttackers(List.of(0, 1));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, elf));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(countPermanents(player1, "Kinscaer Sentry")).isEqualTo(1);
    }

    @Test
    void resolvesAfterSentryLeavesTheBattlefieldUsingTheRemainingAttackers() {
        Permanent sentry = addCreatureReady(player1, new KinscaerSentry());
        addCreatureReady(player1, new IronShieldElf());
        addCreatureReady(player1, new IronShieldElf());
        KinscaerSentry card = new KinscaerSentry();
        harness.setHand(player1, List.of(card));

        declareAttackers(List.of(0, 1, 2));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sentry));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Kinscaer Sentry");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void excludesNoncreaturesAndPutsOnlyOneCreatureOntoTheBattlefield() {
        addCreatureReady(player1, new KinscaerSentry());
        addCreatureReady(player1, new IronShieldElf());
        addCreatureReady(player1, new IronShieldElf());
        OkoLorwynLiege oko = new OkoLorwynLiege();
        IronShieldElf first = new IronShieldElf();
        IronShieldElf second = new IronShieldElf();
        harness.setHand(player1, List.of(oko, first, second));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(oko, second);
        assertThat(countPermanents(player1, "Iron-Shield Elf")).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void laterSentryTriggerCountsCreaturesPutOntoTheBattlefieldAttacking() {
        addCreatureReady(player1, new KinscaerSentry());
        addCreatureReady(player1, new KinscaerSentry());
        addCreatureReady(player1, new KinscaerSentry());
        IronShieldElf elf = new IronShieldElf();
        Luminollusk luminollusk = new Luminollusk();
        harness.setHand(player1, List.of(elf, luminollusk));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);
        Permanent entered = findPermanent(player1, "Luminollusk");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandDoesNotRequireAChoice() {
        addCreatureReady(player1, new KinscaerSentry());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
