package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnterTheGodEternals.class, GrizzlyBears.class, HealingSalve.class})
class EnterTheGodEternalsTest extends BaseCardTest {

    @Test
    @DisplayName("deals 4 damage, gains life equal to damage dealt, mills four, and amasses four")
    void resolvesAllEffects() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, bear);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        castEnterTheGodEternals(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 4);
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("gains life only for damage that is not prevented")
    void lifeGainUsesActualDamage() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, bear);

        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castEnterTheGodEternals(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("adds counters to an existing Army instead of creating a token")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        castEnterTheGodEternals(target);

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    void requiresAcreatureAndPlayerTarget() {
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId(), List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEnterTheGodEternals(Permanent creatureTarget) {
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();
        harness.castSorcery(player1, 0, creatureTarget.getId(), List.of(player2.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
