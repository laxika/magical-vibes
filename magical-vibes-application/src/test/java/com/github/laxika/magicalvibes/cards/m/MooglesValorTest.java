package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MooglesValor.class, DoomBlade.class, GrizzlyBears.class})
class MooglesValorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Moogle for each creature you control with lifelink")
    void createsMoogleForEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

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
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new MooglesValor()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castInstant(player, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void destroy(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> moogles(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> "Moogle".equals(permanent.getCard().getName()))
                .toList();
    }
}
