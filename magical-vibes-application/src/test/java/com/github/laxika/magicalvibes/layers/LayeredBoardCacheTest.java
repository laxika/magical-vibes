package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.cards.e.ElvishChampion;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService.LayeredBoardState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavior tests for the per-{@code GameData} layered board cache (see
 * {@code agent-docs/LAYER_SYSTEM.md} "Board cache"): the finished {@code LayeredBoardState} is
 * memoized in {@code GameData.layeredBoardCache} and invalidated by a structural fingerprint of
 * every input the pass reads — so DIRECT mutations (test setups bypassing the engine's funnels)
 * must invalidate it, and AI simulation copies must start with a cold cache that never leaks
 * back into the real game's.
 */
class LayeredBoardCacheTest extends BaseCardTest {

    private Permanent add(Player player, Card card) {
        card.setOwnerId(player.getId());
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    void unchangedBoardReusesTheCachedEntryAcrossQueries() {
        add(player1, new GloriousAnthem());
        Permanent bear = add(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        Object cachedAfterFirstQuery = gd.layeredBoardCache;
        assertThat(cachedAfterFirstQuery).isNotNull();

        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gd.layeredBoardCache).isSameAs(cachedAfterFirstQuery);
    }

    @Test
    void directBattlefieldListMutationInvalidates() {
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        Permanent anthem = add(player1, new GloriousAnthem());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(anthem);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    void newSubtypeGrantInvalidates() {
        // The Elf lord's filter verdict lives in the cached board — a new floating subtype grant
        // on the bear must invalidate it, or the bear would keep missing the lord's boost.
        add(player1, new ElvishChampion());
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FORESTWALK)).isFalse();

        PermanentGrants.subtype(gd, bear, CardSubtype.ELF);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    void directMutableCardFieldMutationInvalidates() {
        add(player1, new ElvishChampion());
        Permanent bear = add(player1, new GrizzlyBears());
        Card mutableBear = TestCards.mutableCard(bear);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FORESTWALK)).isFalse();

        mutableBear.setSubtypes(List.of(CardSubtype.ELF));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    void abilityAddedInPlaceToAnUnfrozenCardInvalidates() {
        // Only an unfrozen card is hashed beyond its identity, so an ability baked into one in
        // place (a clone's "except it has ..." runtime copy) must still invalidate the board.
        Permanent bear = add(player1, new GrizzlyBears());
        Card mutableBear = TestCards.mutableCard(bear);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();

        mutableBear.addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    void abilityReplacedInPlaceOnAnUnfrozenCardInvalidates() {
        // The card keeps the same number of abilities, so only their identities show the change.
        Permanent bear = add(player1, new GrizzlyBears());
        Card mutableBear = TestCards.mutableCard(bear);
        mutableBear.addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        mutableBear.removeEffectsIf(GrantKeywordEffect.class::isInstance);
        mutableBear.addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void swappingInAnotherFrozenCardInvalidates() {
        // A frozen card is hashed by identity alone, which is exactly what an L1 copy swap changes.
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ELF)).isFalse();

        Card champion = new ElvishChampion();
        champion.freeze();
        bear.setCard(champion);

        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ELF)).isTrue();
    }

    @Test
    void directAttachmentMutationInvalidates() {
        Permanent bear = add(player1, new GrizzlyBears());
        Permanent dub = add(player1, new Dub());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();

        dub.setAttachedTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void floatingEffectAddAndExpiryInvalidate() {
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();

        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test Grant",
                null, player1.getId(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                bear.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        gd.rulesEvents.occur(new TurnEvent.Cleanup(gd.activePlayerId));
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    void conditionalStaticGrantsToggleWhenLifeTotalInvalidatesTheBoard() {
        Permanent ascendant = add(player1, new com.github.laxika.magicalvibes.cards.s.SerraAscendant());

        gd.playerLifeTotals.put(player1.getId(), 30);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        Object cachedBoard = gd.layeredBoardCache;
        assertThat(cachedBoard).isNotNull();

        gd.playerLifeTotals.put(player1.getId(), 20);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gd.layeredBoardCache).isNotSameAs(cachedBoard);
    }

    @Test
    void simulationCopyStartsColdAndNeverSharesTheCache() {
        add(player1, new GloriousAnthem());
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        Object realGameCache = gd.layeredBoardCache;
        assertThat(realGameCache).isNotNull();

        GameData copy = gd.simulationCopy();
        assertThat(copy.layeredBoardCache).isNull();

        // Diverge the copy: the anthem dies in the simulation only.
        copy.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Glorious Anthem"));
        Permanent copiedBear = copy.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bear.getId()))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(copy, copiedBear)).isEqualTo(2);
        assertThat(copy.layeredBoardCache).isNotSameAs(realGameCache);

        // The real game still sees the anthem and still serves its own cache.
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gd.layeredBoardCache).isSameAs(realGameCache);
    }

    /**
     * Runs {@code query} in a fresh pass over the state with {@code player}'s hand at {@code handIndex} left out, then
     * puts the hand and the cache back, the way a spell's cost is previewed as it leaves its hand.
     */
    private <T> T withoutHandCard(Player player, int handIndex, Supplier<T> query) {
        List<Card> hand = gd.playerHands.get(player.getId());
        List<Card> preview = new ArrayList<>(hand);
        preview.remove(handIndex);
        Object cache = gd.layeredBoardCache;
        gd.playerHands.put(player.getId(), preview);
        try {
            return gqs.withFreshQueryScope(gd, query);
        } finally {
            gd.playerHands.put(player.getId(), hand);
            gd.layeredBoardCache = cache;
        }
    }

    @Test
    void freshPassesInOneScopeOverTheSamePreviewedStateShareOneBoard() {
        add(player1, new GloriousAnthem());
        Permanent bear = add(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        gqs.withQueryScope(gd, () -> {
            LayeredBoardState first = withoutHandCard(player1, 0, () -> LayerSystemService.activePass().board());
            LayeredBoardState second = withoutHandCard(player1, 1, () -> LayerSystemService.activePass().board());
            assertThat(second).isSameAs(first);
            assertThat(withoutHandCard(player1, 1, () -> gqs.getEffectivePower(gd, bear))).isEqualTo(3);
            return null;
        });
    }

    @Test
    void aFreshPassInAScopeOverAChangedBoardComputesItsOwn() {
        Permanent anthem = add(player1, new GloriousAnthem());
        Permanent bear = add(player1, new GrizzlyBears());
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        gqs.withQueryScope(gd, () -> {
            battlefield.remove(anthem);
            Object cache = gd.layeredBoardCache;
            try {
                assertThat(gqs.withFreshQueryScope(gd, () -> gqs.getEffectivePower(gd, bear))).isEqualTo(2);
            } finally {
                battlefield.add(0, anthem);
                gd.layeredBoardCache = cache;
            }
            assertThat(gqs.withFreshQueryScope(gd, () -> gqs.getEffectivePower(gd, bear))).isEqualTo(3);
            return null;
        });
    }

    @Test
    void aFreshPassInAScopeJudgesBoardWideQuestionsAgainstItsOwnState() {
        Permanent mindStone = add(player1, new MindStone());
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        gqs.withQueryScope(gd, () -> {
            assertThat(gqs.isCreature(gd, mindStone)).isFalse();
            Permanent march = add(player1, new MarchOfTheMachines());
            Object cache = gd.layeredBoardCache;
            try {
                assertThat(gqs.withFreshQueryScope(gd, () -> gqs.isCreature(gd, mindStone))).isTrue();
            } finally {
                battlefield.remove(march);
                gd.layeredBoardCache = cache;
            }
            return null;
        });
        assertThat(gqs.isCreature(gd, mindStone)).isFalse();
    }
}
