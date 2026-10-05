package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CircuitousRoute;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlanarNexus.class, CircuitousRoute.class})
class PlanarNexusTest extends BaseCardTest {

    @Test
    void isEveryNonbasicLandType() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new PlanarNexus());

        assertThat(gqs.effectiveLandTypes(gd, nexus)).containsExactlyInAnyOrder(
                CardSubtype.CAVE,
                CardSubtype.DESERT,
                CardSubtype.GATE,
                CardSubtype.LAIR,
                CardSubtype.LOCUS,
                CardSubtype.MINE,
                CardSubtype.PLANET,
                CardSubtype.POWER_PLANT,
                CardSubtype.SPHERE,
                CardSubtype.TOWER,
                CardSubtype.TOWN,
                CardSubtype.URZAS
        );
    }

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new PlanarNexus());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void paysOneToTapForAnyColor() {
        harness.addToBattlefield(player1, new PlanarNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void convertsGenericManaToEachOtherColorWithoutUsingTheStack(ManaColor color) {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new PlanarNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(nexus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateColorFilteringWithoutPayingOneMana() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new PlanarNexus());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(nexus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseTheColorlessManaAbilityTwiceWithoutUntapping() {
        harness.addToBattlefield(player1, new PlanarNexus());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeFoundAsAGateWhileInTheLibrary() {
        PlanarNexus nexus = new PlanarNexus();
        harness.setLibrary(player1, List.of(nexus));
        harness.castFromHand(player1, new CircuitousRoute(), "{3}{G}");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(nexus);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Planar Nexus");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
    }
}
