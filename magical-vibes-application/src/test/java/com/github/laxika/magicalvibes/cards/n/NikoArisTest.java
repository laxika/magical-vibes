package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NikoAris.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class NikoArisTest extends BaseCardTest {

    @Test
    void castWithXCreatesShardTokens() {
        harness.setHand(player1, List.of(new NikoAris()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> shards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(shards).hasSize(1);
        assertThat(shards.getFirst().getCard().getType()).isEqualTo(CardType.ENCHANTMENT);
    }

    @Test
    void shardSacrificesToScryAndDraw() {
        harness.setHand(player1, List.of(new NikoAris()));
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int shardIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Shard"));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, shardIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Shard");
    }

    @Test
    void plusOneReturnsTheCreatureAfterItDealsDamage() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void minusOneDealsTwoDamagePerCardDrawnToTappedCreature() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    private Permanent addReadyNiko(Player player, int loyalty) {
        Permanent permanent = new Permanent(new NikoAris());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
