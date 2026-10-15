package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ARealmReborn.class, Forest.class, GrizzlyBears.class})
class ARealmRebornTest extends BaseCardTest {

    @Test
    void otherControlledLandCanTapForAnyColor() {
        harness.addToBattlefield(player1, new ARealmReborn());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void otherControlledNonlandPermanentCanTapForAnyColor() {
        harness.addToBattlefield(player1, new ARealmReborn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void sourceDoesNotGainItsOwnAbility() {
        harness.addToBattlefield(player1, new ARealmReborn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void opponentPermanentsDoNotGainTheAbility() {
        harness.addToBattlefield(player1, new ARealmReborn());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void twoCopiesGrantEachOtherManaAbilities(ManaColor color) {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ARealmReborn());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ARealmReborn());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        harness.addToBattlefield(player1, new ARealmReborn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void grantedManaAbilityCannotBeActivatedAgainWithoutUntapping() {
        harness.addToBattlefield(player1, new ARealmReborn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
