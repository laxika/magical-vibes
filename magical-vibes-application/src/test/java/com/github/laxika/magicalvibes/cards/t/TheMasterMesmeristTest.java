package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrehistoricPet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMasterMesmerist.class, GrizzlyBears.class, AirElemental.class, PrehistoricPet.class, Forest.class})
class TheMasterMesmeristTest extends BaseCardTest {

    @Test
    @DisplayName("The tap ability grants skulk and goads a legal opposing creature")
    void tapAbilityGrantsSkulkAndGoadsTarget() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(master.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.SKULK)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The tap ability rejects creatures above the Master's power")
    void tapAbilityRejectsCreatureWithGreaterPower() {
        addCreatureReady(player1, new TheMasterMesmerist());
        Permanent target = addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with skulk dealing combat damage puts a counter on the Master and draws")
    void skulkCreatureCombatDamagePutsCounterAndDraws() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = addCreatureReady(player1, new PrehistoricPet());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage from a creature without skulk does not trigger")
    void nonSkulkCreatureCombatDamageDoesNotTrigger() {
        Permanent master = addCreatureReady(player1, new TheMasterMesmerist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
