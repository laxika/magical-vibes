package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NestingWurm;
import com.github.laxika.magicalvibes.cards.n.NetterEnDal;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dominate.class, NetterEnDal.class, NestingWurm.class, SealOfRemoval.class})
class DominateTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of a creature with mana value X or less")
    void gainsControlOfCreatureWithinManaValueLimit() {
        harness.addToBattlefield(player2, new NetterEnDal());
        UUID netterEnDalId = harness.getPermanentId(player2, "Netter en-Dal");

        castDominate(1, netterEnDalId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(netterEnDalId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(netterEnDalId));
        assertThat(gd.newestControlEffectFor(netterEnDalId).duration()).isEqualTo(EffectDuration.PERMANENT);
    }

    @Test
    @DisplayName("Can gain control of a creature with lower mana value than X")
    void gainsControlOfLowerManaValueCreature() {
        harness.addToBattlefield(player2, new NetterEnDal());
        UUID netterEnDalId = harness.getPermanentId(player2, "Netter en-Dal");

        castDominate(2, netterEnDalId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(netterEnDalId));
    }

    @Test
    @DisplayName("Rejects a creature with mana value greater than X")
    void rejectsCreatureAboveManaValueLimit() {
        harness.addToBattlefield(player2, new NestingWurm());
        UUID wurmId = harness.getPermanentId(player2, "Nesting Wurm");

        assertThatThrownBy(() -> castDominate(2, wurmId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature permanent")
    void rejectsNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SealOfRemoval());
        UUID sealId = harness.getPermanentId(player2, "Seal of Removal");

        assertThatThrownBy(() -> castDominate(1, sealId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDominate(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 3);
        harness.castInstant(player1, 0, xValue, targetId);
    }
}
