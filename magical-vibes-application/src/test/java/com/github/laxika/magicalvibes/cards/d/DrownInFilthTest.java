package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownInFilth.class, Forest.class, GrizzlyBears.class})
class DrownInFilthTest extends BaseCardTest {

    private void castWithLibrary(List<Card> library, UUID targetId) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DrownInFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Four milled lands give -4/-4 and kill a 2/2")
    void millsFourLandsAndKillsBear() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        castWithLibrary(List.of(new Forest(), new Forest(), new Forest(), new Forest()), bearId);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only lands among the milled cards count, and the debuff wears off at cleanup")
    void countsOnlyLandsAndWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearId = bear.getId();

        castWithLibrary(
                List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()), bearId);

        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast with an invalid target")
    void cannotCastWithInvalidTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DrownInFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    void countsPreExistingLandsEvenWithAnEmptyLibrary() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithLibrary(List.of(), bear.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    void millsOnlyAvailableCardsFromAShortLibrary() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();

        castWithLibrary(List.of(land, nonland), bear.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, nonland);
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void ignoresOpponentsLandsAndLeavesCreatureUnchangedWhenControllerHasNone() {
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new Forest());

        castWithLibrary(library, bear.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(library.subList(0, 4));
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void debuffDoesNotChangeWhenGraveyardChangesAfterResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithLibrary(List.of(new Forest()), bear.getId());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));

        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
        harness.setGraveyard(player1, List.of());
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void doesNotMillWhenTheOnlyTargetLeavesBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DrownInFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.setGraveyard(player2, List.of(bear.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Drown in Filth");
    }
}
