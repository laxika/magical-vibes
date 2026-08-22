package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZephidsEmbrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErietteTheBeguiler.class, ZephidsEmbrace.class, GrizzlyBears.class, AirElemental.class})
class ErietteTheBeguilerTest extends BaseCardTest {

    @Test
    void eligibleAuraStealsOpponentPermanentUntilAuraDetaches() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ZephidsEmbrace)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void auraDoesNotTriggerForHigherManaValueOpponentPermanent() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new AirElemental());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    private void castZephidsEmbrace(Permanent target) {
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, target.getId());
    }
}
