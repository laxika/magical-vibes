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
        UUID netterEnDalId = harness.addToBattlefieldAndReturn(player2, new NetterEnDal()).getId();

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
        UUID netterEnDalId = harness.addToBattlefieldAndReturn(player2, new NetterEnDal()).getId();

        castDominate(2, netterEnDalId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(netterEnDalId));
    }

    @Test
    @DisplayName("Rejects a creature with mana value greater than X")
    void rejectsCreatureAboveManaValueLimit() {
        UUID wurmId = harness.addToBattlefieldAndReturn(player2, new NestingWurm()).getId();

        assertThatThrownBy(() -> castDominate(2, wurmId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature permanent")
    void rejectsNoncreaturePermanent() {
        UUID sealId = harness.addToBattlefieldAndReturn(player2, new SealOfRemoval()).getId();

        assertThatThrownBy(() -> castDominate(1, sealId))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Can target a creature you already control")
    void canTargetOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new NetterEnDal()).getId();

        castDominate(1, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertInGraveyard(player1, "Dominate");
    }

    @Test
    @DisplayName("X equal to zero cannot target a creature with positive mana value")
    void zeroXRejectsPositiveManaValue() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NetterEnDal()).getId();

        assertThatThrownBy(() -> castDominate(0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain control if the target is returned to hand in response")
    void targetReturnedToHandBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NetterEnDal()).getId();
        harness.addToBattlefield(player2, new SealOfRemoval());

        castDominate(1, targetId);
        harness.activateAbility(player2, 1, null, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Netter en-Dal");
        harness.assertNotOnBattlefield(player2, "Netter en-Dal");
        harness.assertInHand(player2, "Netter en-Dal");
        harness.assertInGraveyard(player1, "Dominate");
        assertThat(gd.stack).isEmpty();
    }

    private void castDominate(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 3);
        harness.castInstant(player1, 0, xValue, targetId);
    }
}
