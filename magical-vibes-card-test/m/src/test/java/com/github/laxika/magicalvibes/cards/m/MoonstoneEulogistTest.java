package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonstoneEulogist.class, GrizzlyBears.class, Shock.class})
class MoonstoneEulogistTest extends BaseCardTest {

    @Test
    void createsBloodWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new MoonstoneEulogist());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void sacrificingArtifactAddsCounterAndLife() {
        Permanent eulogist = harness.addToBattlefieldAndReturn(player1, new MoonstoneEulogist());
        Permanent artifact = addPermanent(player1, CardType.ARTIFACT);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        sacrifice(artifact);
        resolveAllTriggers();

        assertThat(eulogist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void sacrificingNonArtifactDoesNotTrigger() {
        Permanent eulogist = harness.addToBattlefieldAndReturn(player1, new MoonstoneEulogist());
        Permanent creature = addPermanent(player1, CardType.CREATURE);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        sacrifice(creature);
        resolveAllTriggers();

        assertThat(eulogist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addPermanent(Player player, CardType type) {
        Card card = new Card();
        card.setType(type);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void sacrifice(Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), card));
    }
}
