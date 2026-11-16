package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.cards.a.Anger;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Behavior tests for the static-ability sources a layered pass collects once and shares with every static-bonus
 * assembly in it ({@code LayerSystemService.Pass#staticSources}): what depends on the permanent being assembled — its
 * controller, and so whether a "creatures you control" source applies to it — is still judged per permanent, and a
 * scope that changes the game state under a cached collection fails in test JVMs instead of reading stale sources.
 */
@CardUsed({Anger.class, GloriousAnthem.class, GrizzlyBears.class, Mountain.class})
class StaticSourceCacheTest extends BaseCardTest {

    private Permanent add(Player player, Card card) {
        card.setOwnerId(player.getId());
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    void sharedSourcesStillApplyOnlyToTheirControllersCreatures() {
        add(player1, new GloriousAnthem());
        Permanent ownBear = add(player1, new GrizzlyBears());
        Permanent opponentBear = add(player2, new GrizzlyBears());

        List<Integer> powers = gqs.withQueryScope(gd, () -> List.of(
                gqs.getEffectivePower(gd, opponentBear),
                gqs.getEffectivePower(gd, ownBear),
                gqs.getEffectivePower(gd, opponentBear)));

        assertThat(powers).containsExactly(2, 3, 2);
    }

    @Test
    void sharedSourcesBoostEveryCreatureAssembledInOneScope() {
        add(player1, new GloriousAnthem());
        List<Permanent> bears = List.of(
                add(player1, new GrizzlyBears()), add(player1, new GrizzlyBears()), add(player1, new GrizzlyBears()));

        List<Integer> toughnesses = gqs.withQueryScope(gd, () -> bears.stream()
                .map(bear -> gqs.getEffectiveToughness(gd, bear)).toList());

        assertThat(toughnesses).containsExactly(3, 3, 3);
    }

    @Test
    void sharedGraveyardSourceAppliesOnlyToItsOwnersCreatures() {
        Card anger = new Anger();
        anger.setOwnerId(player1.getId());
        gd.playerGraveyards.get(player1.getId()).add(anger);
        add(player1, new Mountain());
        Permanent ownBear = add(player1, new GrizzlyBears());
        Permanent otherOwnBear = add(player1, new GrizzlyBears());
        Permanent opponentBear = add(player2, new GrizzlyBears());

        List<Boolean> haste = gqs.withQueryScope(gd, () -> List.of(
                gqs.hasKeyword(gd, ownBear, Keyword.HASTE),
                gqs.hasKeyword(gd, opponentBear, Keyword.HASTE),
                gqs.hasKeyword(gd, otherOwnBear, Keyword.HASTE)));

        assertThat(haste).containsExactly(true, false, true);
    }

    @Test
    void sourcesAreCollectedAgainInTheNextScope() {
        Permanent bear = add(player1, new GrizzlyBears());
        assertThat(gqs.withQueryScope(gd, () -> gqs.getEffectivePower(gd, bear))).isEqualTo(2);

        add(player1, new GloriousAnthem());

        assertThat(gqs.withQueryScope(gd, () -> gqs.getEffectivePower(gd, bear))).isEqualTo(3);
    }

    @Test
    void changingTheBoardInsideOneScopeFailsTheVerification() {
        assumeTrue(Boolean.getBoolean("verifyStaticSourceCache"), "test JVMs run with -DverifyStaticSourceCache=true");
        Permanent bear = add(player1, new GrizzlyBears());
        Permanent otherBear = add(player1, new GrizzlyBears());

        assertThatThrownBy(() -> gqs.withQueryScope(gd, () -> {
            gqs.getEffectivePower(gd, bear);
            add(player1, new GloriousAnthem());
            return gqs.getEffectivePower(gd, otherBear);
        })).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("static-ability sources changed inside one layered pass");
    }
}
