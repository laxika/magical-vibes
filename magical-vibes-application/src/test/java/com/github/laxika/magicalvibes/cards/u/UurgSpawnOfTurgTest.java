package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.y.YavimayaSojourner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UurgSpawnOfTurg.class, Forest.class, Plains.class, YavimayaSojourner.class})
class UurgSpawnOfTurgTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals land cards in its controller's graveyard and toughness is 5")
    void powerCountsOwnGraveyardLands() {
        Permanent uurg = harness.addToBattlefieldAndReturn(player1, new UurgSpawnOfTurg());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains(), new YavimayaSojourner()));
        harness.setGraveyard(player2, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, uurg)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, uurg)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).add(new Forest());
        assertThat(gqs.getEffectivePower(gd, uurg)).isEqualTo(3);
    }

    @Test
    @DisplayName("Surveils 1 at the beginning of its controller's upkeep")
    void surveilsOneAtUpkeep() {
        harness.addToBattlefield(player1, new UurgSpawnOfTurg());
        Card topCard = new YavimayaSojourner();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Yavimaya Sojourner");
    }

    @Test
    @DisplayName("Pays {B}{G}, sacrifices a land, and gains 2 life")
    void sacrificesLandAndGainsLife() {
        harness.addToBattlefield(player1, new UurgSpawnOfTurg());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new UurgSpawnOfTurg());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May leave the surveilled card on top without changing power")
    void mayKeepSurveilledLand() {
        Permanent uurg = harness.addToBattlefieldAndReturn(player1, new UurgSpawnOfTurg());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Plains()));
        harness.setGraveyard(player1, List.of());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        });

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, uurg)).isZero();
    }

    @Test
    @DisplayName("Surveilling a land increases power immediately")
    void surveilledLandIncreasesPower() {
        Permanent uurg = harness.addToBattlefieldAndReturn(player1, new UurgSpawnOfTurg());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Plains()));
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gqs.getEffectivePower(gd, uurg)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotSurveilAtOpponentsUpkeep() {
        harness.addToBattlefield(player1, new UurgSpawnOfTurg());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Surveil resolves harmlessly with an empty library")
    void surveilsEmptyLibrary() {
        harness.addToBattlefield(player1, new UurgSpawnOfTurg());
        harness.setLibrary(player1, List.of());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        });

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, increases power, and can use a tapped land")
    void sacrificeIsAnImmediateCost() {
        Permanent uurg = harness.addToBattlefieldAndReturn(player1, new UurgSpawnOfTurg());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        harness.setGraveyard(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, uurg)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }
}
