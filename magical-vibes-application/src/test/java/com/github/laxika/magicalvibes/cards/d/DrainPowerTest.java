package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrainPower.class, Forest.class, GrizzlyBears.class, Island.class})
class DrainPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target player's lands and controller adds the mana they produce")
    void drainsLandMana() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());

        cast();

        assertThat(battlefield).allMatch(Permanent::isTapped);
        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Mana the target player already had is also drained to the controller")
    void drainsPreexistingMana() {
        harness.addMana(player2, ManaColor.RED, 3);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Returns land mana to the controller when targeting themself")
    void returnsLandManaWhenTargetingSelf() {
        harness.addToBattlefield(player1, new Forest());

        cast(player1.getId());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Already-tapped lands and non-lands produce nothing")
    void ignoresTappedAndNonLands() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        forest.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getLast();

        cast();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Preserves spending restrictions on transferred mana")
    void preservesManaRestrictions() {
        ManaPool targetPool = gd.playerManaPools.get(player2.getId());
        targetPool.addAbilityOnlyMana(ManaColor.COLORLESS, 2);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @CardUsed({CityOfBrass.class})
    @DisplayName("Lets the target choose the color from an any-color land")
    void promptsTargetForAnyColorLand() {
        harness.addToBattlefield(player2, new CityOfBrass());

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @CardUsed({CabalCoffers.class, Swamp.class})
    @DisplayName("Activates a land mana ability with a mana activation cost")
    void activatesPaidLandManaAbility() {
        harness.addToBattlefield(player2, new CabalCoffers());
        harness.addToBattlefield(player2, new Swamp());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isTapped);
    }

    private void cast() {
        cast(player2.getId());
    }

    private void cast(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DrainPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
