package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.cards.s.SyphonSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaweftSliver.class, SyphonSliver.class, KalonianTusker.class})
class ManaweftSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Manaweft Sliver grants itself the tap-for-any-color mana ability")
    void grantsAbilityToItself() {
        Permanent manaweft = addCreatureReady(player1, new ManaweftSliver());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(manaweft.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other Slivers you control gain the mana ability")
    void grantsAbilityToOtherSlivers() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        Permanent syphon = addCreatureReady(player1, new SyphonSliver());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(syphon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted mana ability obeys summoning sickness")
    void grantedAbilityObeysSummoningSickness() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new SyphonSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Non-Sliver creatures you control do not gain the mana ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new KalonianTusker());
        Permanent tusker = gd.playerBattlefields.get(player1.getId()).get(1);

        assertThat(gqs.computeStaticBonus(gd, tusker).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Opponent's Slivers do not gain the mana ability")
    void doesNotGrantAbilityToOpponentSlivers() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player2, new SyphonSliver());
        Permanent opponentSliver = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThat(gqs.computeStaticBonus(gd, opponentSliver).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Granted mana ability goes away when Manaweft Sliver leaves the battlefield")
    void abilityRemovedWhenManaweftLeaves() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new SyphonSliver());
        Permanent manaweft = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent syphon = gd.playerBattlefields.get(player1.getId()).get(1);

        gd.playerBattlefields.get(player1.getId()).remove(manaweft);

        assertThat(gqs.computeStaticBonus(gd, syphon).grantedActivatedAbilities()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A granted mana ability adds exactly one mana of any chosen color without using the stack")
    void producesEachColor(ManaColor color) {
        harness.addToBattlefield(player1, new ManaweftSliver());
        Permanent sliver = addCreatureReady(player1, new SyphonSliver());

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        assertThat(sliver.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Sliver cannot activate its granted mana ability again")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new ManaweftSliver());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Manaweft Sliver still grants the ability to a ready Sliver")
    void summoningSickSourceGrantsAbility() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        addCreatureReady(player1, new SyphonSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
