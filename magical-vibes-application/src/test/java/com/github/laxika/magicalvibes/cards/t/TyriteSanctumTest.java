package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AegarTheFreezingFlame;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KolvoriGodOfKinship;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TyriteSanctum.class, AegarTheFreezingFlame.class,
        GrizzlyBears.class, KolvoriGodOfKinship.class})
class TyriteSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Tyrite Sanctum produces colorless mana")
    void tappingProducesColorlessMana() {
        addSanctum(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability makes a legendary creature a God and puts a +1/+1 counter on it")
    void makesLegendaryCreatureAGodAndAddsCounter() {
        addSanctum(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AegarTheFreezingFlame());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.GOD);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The third ability sacrifices Tyrite Sanctum and puts an indestructible counter on a God")
    void sacrificesSanctumAndAddsIndestructibleCounter() {
        Permanent sanctum = addSanctum(player1);
        Permanent god = harness.addToBattlefieldAndReturn(player1, new KolvoriGodOfKinship());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, god.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sanctum);
        assertThat(god.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, god, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The second ability cannot target a nonlegendary creature")
    void secondAbilityRejectsNonlegendaryCreature() {
        addSanctum(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("The third ability cannot target a non-God")
    void thirdAbilityRejectsNonGod() {
        addSanctum(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AegarTheFreezingFlame());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("God");
    }

    private Permanent addSanctum(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TyriteSanctum());
    }
}
