package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.w.WatcherSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GemhideSliver.class, WatcherSliver.class, AshcoatBear.class})
class GemhideSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers, including opposing Slivers and Gemhide Sliver itself, gain the mana ability")
    void grantsManaAbilityToAllSlivers() {
        Permanent gemhide = addCreatureReady(player1, new GemhideSliver());
        Permanent friendlySliver = addCreatureReady(player1, new WatcherSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WatcherSliver());

        activateForColor(player1, gemhide, "BLUE");
        activateForColor(player1, friendlySliver, "GREEN");
        activateForColor(player2, opposingSliver, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gemhide Sliver does not grant the mana ability to non-Slivers")
    void doesNotGrantManaAbilityToNonSlivers() {
        addCreatureReady(player1, new GemhideSliver());
        Permanent bears = addCreatureReady(player1, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bears), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted mana ability still obeys summoning sickness")
    void grantedManaAbilityObeysSummoningSickness() {
        addCreatureReady(player1, new GemhideSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new WatcherSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(sliver), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("The granted mana ability disappears when Gemhide Sliver leaves the battlefield")
    void removesGrantedManaAbilityWhenSourceLeavesBattlefield() {
        Permanent gemhide = addCreatureReady(player1, new GemhideSliver());
        Permanent sliver = addCreatureReady(player1, new WatcherSliver());
        gd.playerBattlefields.get(player1.getId()).remove(gemhide);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(sliver), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each of the five colors can be produced immediately by tapping a Sliver")
    void producesEachColorWithoutUsingTheStack() {
        addCreatureReady(player1, new GemhideSliver());
        for (ManaColor color : new ManaColor[]{ManaColor.WHITE, ManaColor.BLUE,
                ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN}) {
            Permanent sliver = addCreatureReady(player1, new WatcherSliver());

            activateForColor(player1, sliver, color.name());

            assertThat(sliver.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
            assertThatThrownBy(() -> harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(sliver), null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Multiple Gemhide Slivers do not multiply the mana from one activation")
    void multipleSourcesStillProduceOneManaPerActivation() {
        Permanent first = addCreatureReady(player1, new GemhideSliver());
        addCreatureReady(player1, new GemhideSliver());
        Permanent sliver = addCreatureReady(player1, new WatcherSliver());

        activateForColor(player1, sliver, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).remove(first);
        Permanent anotherSliver = addCreatureReady(player1, new WatcherSliver());

        activateForColor(player1, anotherSliver, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
    private void activateForColor(com.github.laxika.magicalvibes.model.Player player,
                                  Permanent permanent, String color) {
        harness.activateAbility(player,
                gd.playerBattlefields.get(player.getId()).indexOf(permanent), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player, color);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
