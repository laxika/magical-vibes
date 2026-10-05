package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoobbueGardener;
import com.github.laxika.magicalvibes.cards.s.SephirothsIntervention;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MooglesValor.class, SephirothsIntervention.class, GoobbueGardener.class})
class MooglesValorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Moogle for each creature you control with lifelink")
    void createsMoogleForEachCreature() {
        harness.addToBattlefield(player1, new GoobbueGardener());
        harness.addToBattlefield(player1, new GoobbueGardener());

        cast(player1);

        assertThat(moogles(player1)).hasSize(2).allSatisfy(moogle -> {
            assertThat(moogle.getCard().getPower()).isEqualTo(1);
            assertThat(moogle.getCard().getToughness()).isEqualTo(2);
            assertThat(moogle.getCard().getSubtypes()).contains(CardSubtype.MOOGLE);
            assertThat(moogle.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
    }

    @Test
    @DisplayName("Newly created Moogles and existing creatures gain indestructible")
    void allOwnCreaturesGainIndestructible() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());

        cast(player1);

        for (Permanent permanent : List.of(bear, moogles(player1).getFirst())) {
            destroy(player2, permanent.getId());
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(bear)
                .contains(moogles(player1).getFirst());
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());

        cast(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    void createsNoTokensWithoutCreatures() {
        cast(player1);

        assertThat(moogles(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(Forest.class)
    void excludesOpposingCreaturesAndNoncreaturePermanents() {
        harness.addToBattlefield(player1, new GoobbueGardener());
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GoobbueGardener());

        cast(player1);

        assertThat(moogles(player1)).hasSize(1);
        assertThat(moogles(player2)).isEmpty();
        destroy(player1, opponentBear.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBear);
    }

    @Test
    void creaturesEnteringAfterResolutionAreNotIndestructible() {
        harness.addToBattlefield(player1, new GoobbueGardener());
        cast(player1);
        Permanent laterBear = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());

        destroy(player2, laterBear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(laterBear);
        assertThat(moogles(player1)).hasSize(1);
    }

    @Test
    void countsCreaturesAtResolutionAfterRemovalInResponse() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());
        harness.addToBattlefield(player1, new GoobbueGardener());
        harness.castFromHand(player1, new MooglesValor(), "{3}{W}{W}");

        destroy(player2, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(moogles(player1)).hasSize(1);
    }

    @Test
    void countsExistingCreatureTokensOnSubsequentCast() {
        harness.addToBattlefield(player1, new GoobbueGardener());
        cast(player1);

        cast(player1);

        assertThat(moogles(player1)).hasSize(3);
    }

    @Test
    void whiteMoogleTokensRemainAfterTurnButLoseIndestructible() {
        harness.addToBattlefield(player1, new GoobbueGardener());
        cast(player1);
        Permanent moogle = moogles(player1).getFirst();
        assertThat(moogle.getCard().getColor()).isEqualTo(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(moogles(player1)).containsExactly(moogle);
        destroy(player2, moogle.getId());
        assertThat(moogles(player1)).isEmpty();
    }

    private void cast(Player player) {
        harness.castFromHand(player, new MooglesValor(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private void destroy(Player player, UUID targetId) {
        harness.setHand(player, List.of(new SephirothsIntervention()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player, 0, targetId);
    }

    private List<Permanent> moogles(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> "Moogle".equals(permanent.getCard().getName()))
                .toList();
    }
}
