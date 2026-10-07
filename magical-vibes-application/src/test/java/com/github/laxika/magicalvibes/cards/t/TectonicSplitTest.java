package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TectonicSplit.class, Forest.class, Naturalize.class})
class TectonicSplitTest extends BaseCardTest {

    @Test
    void sacrificesHalfOfControlledLandsRoundedUp() {
        harness.setHand(player1, List.of(new TectonicSplit()));
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Forest")))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Tectonic Split");
    }

    @Test
    void controlledLandsCanTapForThreeManaOfAnyColor() {
        harness.addToBattlefield(player1, new TectonicSplit());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void sacrificesExactlyHalfWithAnEvenNumberOfLandsBeforeResolution() {
        harness.setHand(player1, List.of(new TectonicSplit()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithSacrifices(player1, 0, null, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Tectonic Split");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Tectonic Split");
    }

    @Test
    void canBeCastWithoutControllingLands() {
        harness.castFromHand(player1, new TectonicSplit(), "{4}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tectonic Split");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotSacrificeTooFewLands() {
        harness.setHand(player1, List.of(new TectonicSplit()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(
                player1, 0, null, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Tectonic Split");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsLand() {
        harness.setHand(player1, List.of(new TectonicSplit()));
        harness.addToBattlefield(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(
                player1, 0, null, List.of(opposingLand.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Tectonic Split");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    void opponentsLandsKeepTheirOrdinaryManaProduction() {
        harness.addToBattlefield(player1, new TectonicSplit());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hexproofPreventsOpponentFromTargetingIt() {
        Permanent split = harness.addToBattlefieldAndReturn(player1, new TectonicSplit());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, split.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tectonic Split");
        harness.assertInHand(player2, "Naturalize");
    }

    @Test
    void controllerCanTargetItAndLandsLoseGrantedAbilityWhenItLeaves() {
        Permanent split = harness.addToBattlefieldAndReturn(player1, new TectonicSplit());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, split.getId());
        harness.assertInGraveyard(player1, "Tectonic Split");
        harness.assertNotOnBattlefield(player1, "Tectonic Split");

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }
}
