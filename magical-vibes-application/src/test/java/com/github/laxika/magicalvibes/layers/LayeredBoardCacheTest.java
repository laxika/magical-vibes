package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.cards.e.ElvishChampion;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
    void directPermanentFieldMutationInvalidates() {
        // The Elf lord's filter verdict lives in the cached board — a direct subtype grant on
        // the bear must invalidate it, or the bear would keep missing the lord's boost.
        add(player1, new ElvishChampion());
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FORESTWALK)).isFalse();

        bear.getGrantedSubtypes().add(CardSubtype.ELF);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FORESTWALK)).isTrue();
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
                bear.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    void conditionalStaticGrantsToggleWithoutInvalidatingTheBoard() {
        // Conditional wrappers are excluded from the board computation (LayerSystemService
        // skips them in collectInstances): their conditions read volatile inputs — life
        // totals here — that the board fingerprint deliberately does not cover, and the
        // static-bonus assembly evaluates them fresh on every query. The cached board entry
        // must therefore be REUSED across the toggle while the answer still changes.
        Permanent ascendant = add(player1, new com.github.laxika.magicalvibes.cards.s.SerraAscendant());

        gd.playerLifeTotals.put(player1.getId(), 30);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        Object cachedBoard = gd.layeredBoardCache;
        assertThat(cachedBoard).isNotNull();

        gd.playerLifeTotals.put(player1.getId(), 20);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gd.layeredBoardCache).isSameAs(cachedBoard);
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
}
