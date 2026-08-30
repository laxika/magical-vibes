package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CullingDais;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandbenderScavengers.class, CullingDais.class, GrizzlyBears.class, HillGiant.class, Island.class})
class SandbenderScavengersTest extends BaseCardTest {

    @Test
    void sacrificePutsCounterAndDeathCanReturnCreatureUpToLastKnownPower() {
        Card scavengers = new SandbenderScavengers();
        Permanent scavengersPermanent = addCreatureReady(player1, scavengers);
        harness.addToBattlefield(player1, new CullingDais());
        Permanent sacrificedPermanent = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificedPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scavengersPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Card validTarget = new GrizzlyBears();
        Card tooExpensiveTarget = new HillGiant();
        Card nonCreatureTarget = new Island();
        harness.setGraveyard(player1, List.of(validTarget, tooExpensiveTarget, nonCreatureTarget));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scavengersPermanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(validTarget.getId());
        assertThat(choice.validCardIds()).doesNotContain(tooExpensiveTarget.getId(), nonCreatureTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(validTarget.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(scavengers.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(validTarget.getId()));
    }

    @Test
    void decliningExileLeavesSourceInGraveyard() {
        Card scavengers = new SandbenderScavengers();
        Card target = new GrizzlyBears();
        Permanent scavengersPermanent = addCreatureReady(player1, scavengers);
        harness.setGraveyard(player1, List.of(target));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scavengersPermanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(scavengers.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(scavengers.getId(), target.getId());
    }
}
