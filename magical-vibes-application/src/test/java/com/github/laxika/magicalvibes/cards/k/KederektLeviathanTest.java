package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({KederektLeviathan.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Terminate.class})
@DisplayName("Kederekt Leviathan")
class KederektLeviathanTest extends BaseCardTest {

    private void castLeviathan() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB returns all other nonland permanents to their owners' hands")
    void etbReturnsAllOtherNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        castLeviathan();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB does not return lands")
    void etbDoesNotReturnLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castLeviathan();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("ETB does not return Kederekt Leviathan itself")
    void etbDoesNotReturnItself() {
        castLeviathan();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Kederekt Leviathan");
        harness.assertNotInHand(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Unearth returns it to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Kederekt Leviathan");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Unearthed Kederekt Leviathan is exiled at the next end step")
    void unearthExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve unearth (returns it to battlefield)
        harness.passBothPriorities(); // resolve its ETB trigger

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kederekt Leviathan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kederekt Leviathan"));
    }

    @Test
    @DisplayName("Unearth can only be activated at sorcery speed")
    void unearthOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Unearthed Kederekt Leviathan is exiled if it would leave the battlefield")
    void unearthExiledIfWouldLeaveBattlefield() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve unearth
        harness.passBothPriorities(); // resolve ETB

        Permanent perm = findPermanent(player1, "Kederekt Leviathan");

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, perm.getId());

        harness.assertNotOnBattlefield(player1, "Kederekt Leviathan");
        harness.assertNotInGraveyard(player1, "Kederekt Leviathan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kederekt Leviathan"));
    }

    @Test
    @DisplayName("Unearth ETB still returns all other nonland permanents")
    void unearthEtbReturnsOtherNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve unearth
        harness.passBothPriorities(); // resolve ETB

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("ETB returns another Leviathan but preserves the entering one")
    void etbReturnsAnotherLeviathan() {
        KederektLeviathan other = new KederektLeviathan();
        harness.addToBattlefield(player2, other);
        castLeviathan();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kederekt Leviathan");
        assertThat(gd.playerHands.get(player2.getId())).contains(other);
        harness.assertOnBattlefield(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Another Leviathan's ETB exiles an unearthed Leviathan instead of returning it to hand")
    void bounceExilesUnearthedLeviathan() {
        KederektLeviathan unearthed = new KederektLeviathan();
        harness.setGraveyard(player1, List.of(unearthed));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        castLeviathan();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(unearthed);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(unearthed);
        harness.assertNotInGraveyard(player1, "Kederekt Leviathan");
        harness.assertOnBattlefield(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthCannotActivateDuringCombat() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Unearth cannot be activated with a nonempty stack")
    void unearthCannotActivateWithSpellOnStack() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        castLeviathan();
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("Unearth requires blue mana even when seven mana are available")
    void unearthRequiresBlueMana() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kederekt Leviathan");
    }

    @Test
    @DisplayName("ETB still returns other permanents after Leviathan is destroyed")
    void etbResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        castLeviathan();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Kederekt Leviathan"));
        harness.assertInGraveyard(player1, "Kederekt Leviathan");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Unearth cannot be activated with only six mana")
    void unearthRequiresSevenMana() {
        harness.setGraveyard(player1, List.of(new KederektLeviathan()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kederekt Leviathan");
    }
}
