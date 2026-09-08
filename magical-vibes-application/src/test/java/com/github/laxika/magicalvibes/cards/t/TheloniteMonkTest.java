package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenHold;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheloniteMonk.class, Thallid.class, RiverMerfolk.class, DwarvenHold.class})
class TheloniteMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a green creature makes the target land a Forest indefinitely")
    void targetLandBecomesForestIndefinitely() {
        Permanent monk = addReadyMonk();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Thallid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        assertThat(monk.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);

        land.resetModifiers();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Becoming a Forest grants the land a green mana ability")
    void becomingForestGrantsGreenManaAbility() {
        addReadyMonk();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Thallid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        int landIndex = gd.playerBattlefields.get(player1.getId()).indexOf(land);
        harness.tapPermanent(player1, landIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The Monk can sacrifice itself to pay its ability")
    void monkCanSacrificeItself() {
        Permanent monk = addReadyMonk();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(monk);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(monk.getCard());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("The ability cannot target a nonland permanent")
    void cannotTargetNonland() {
        addReadyMonk();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Thallid());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The ability cannot be activated while the Monk is tapped")
    void cannotActivateWhenTapped() {
        Permanent monk = addReadyMonk();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        monk.tap();
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The ability can target a land an opponent controls")
    void canTargetOpponentLand() {
        Permanent monk = addReadyMonk();
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new DwarvenHold());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, opponentLand.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(monk.getCard());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, opponentLand)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Only a green creature can be sacrificed for the ability")
    void onlyGreenCreatureCanBeSacrificed() {
        addReadyMonk();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Thallid());
        Permanent nonGreenCreature = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGreenCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonGreenCreature.getCard());
    }

    private Permanent addReadyMonk() {
        return addCreatureReady(player1, new TheloniteMonk());
    }
}
