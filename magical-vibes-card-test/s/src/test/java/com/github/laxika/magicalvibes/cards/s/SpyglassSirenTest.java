package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SpyglassSiren.class, Island.class})
class SpyglassSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Map token")
    void etbCreatesMapToken() {
        harness.setHand(player1, List.of(new SpyglassSiren()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).singleElement()
                .satisfies(map -> assertThat(map.getCard().hasType(CardType.ARTIFACT)).isTrue())
                .satisfies(map -> assertThat(map.getCard().getSubtypes()).contains(CardSubtype.MAP));
    }

    @Test
    void mapExploringLandPutsItInHandWithoutCounter() {
        Permanent siren = castSiren();
        Island land = new Island();
        harness.setLibrary(player1, List.of(land));

        activateMap(siren);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void mapExploringNonlandCanKeepItOnTop() {
        Permanent siren = castSiren();
        SpyglassSiren revealed = new SpyglassSiren();
        harness.setLibrary(player1, List.of(revealed));

        activateMap(siren);
        assertThat(findPermanents(player1, "Map")).isEmpty();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void mapExploringNonlandCanPutItInGraveyard() {
        Permanent siren = castSiren();
        SpyglassSiren revealed = new SpyglassSiren();
        harness.setLibrary(player1, List.of(revealed));

        activateMap(siren);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(revealed);
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void mapCannotTargetOpponentsCreature() {
        castSiren();
        harness.addToBattlefield(player2, new SpyglassSiren());
        Permanent opponent = findPermanent(player2, "Spyglass Siren");

        assertThatThrownBy(() -> activateMap(opponent)).isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    void mapCannotActivateDuringCombat() {
        Permanent siren = castSiren();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> activateMap(siren)).isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    void etbTriggerResolvesAfterSirenLeavesBattlefield() {
        harness.setHand(player1, List.of(new SpyglassSiren()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent siren = findPermanent(player1, "Spyglass Siren");
        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(siren);
        gd.playerGraveyards.get(player1.getId()).add(siren.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
        assertThat(findPermanents(player2, "Map")).isEmpty();
        assertThat(findPermanents(player1, "Spyglass Siren")).isEmpty();
    }

    private Permanent castSiren() {
        harness.setHand(player1, List.of(new SpyglassSiren()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Spyglass Siren");
    }

    private void activateMap(Permanent target) {
        Permanent map = findPermanent(player1, "Map");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(map),
                null, target.getId());
    }
}
