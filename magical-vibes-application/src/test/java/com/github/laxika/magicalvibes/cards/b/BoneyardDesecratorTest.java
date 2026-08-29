package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneyardDesecrator.class, GrizzlyBears.class})
class BoneyardDesecratorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Boneyard Desecrator")
    void sacrificingAnotherCreaturePutsCounterOnSource() {
        Permanent desecrator = addReadyDesecrator();
        harness.addToBattlefield(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"ASSASSIN", "MERCENARY", "PIRATE", "ROGUE", "WARLOCK"})
    @DisplayName("Sacrificing an outlaw also creates a Treasure token")
    void sacrificingAnOutlawCreatesTreasure(CardSubtype outlawSubtype) {
        Permanent desecrator = addReadyDesecrator();
        GrizzlyBears outlaw = new GrizzlyBears();
        outlaw.setSubtypes(List.of(outlawSubtype));
        harness.addToBattlefield(player1, outlaw);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Boneyard Desecrator cannot sacrifice itself")
    void cannotSacrificeItself() {
        addReadyDesecrator();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDesecrator() {
        Permanent desecrator = new Permanent(new BoneyardDesecrator());
        desecrator.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(desecrator);
        return desecrator;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
