package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({UurgSpawnOfTurg.class, Forest.class, Plains.class, GrizzlyBears.class})
class UurgSpawnOfTurgTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals land cards in its controller's graveyard and toughness is 5")
    void powerCountsOwnGraveyardLands() {
        Permanent uurg = harness.addToBattlefieldAndReturn(player1, new UurgSpawnOfTurg());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains(), new GrizzlyBears()));
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
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
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
}
