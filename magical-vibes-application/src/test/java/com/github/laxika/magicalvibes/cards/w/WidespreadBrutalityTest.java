package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WidespreadBrutality.class, GrizzlyBears.class})
class WidespreadBrutalityTest extends BaseCardTest {

    @Test
    @DisplayName("Amasses a 2/2 Army and damages each non-Army creature")
    void amassesAndDamagesNonArmyCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWidespreadBrutality();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bear.getId()));
    }

    @Test
    @DisplayName("Adds counters to an existing Army before using its power")
    void amassesOnExistingArmyBeforeDamaging() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWidespreadBrutality();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(opponent.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void castWidespreadBrutality() {
        harness.setHand(player1, List.of(new WidespreadBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
