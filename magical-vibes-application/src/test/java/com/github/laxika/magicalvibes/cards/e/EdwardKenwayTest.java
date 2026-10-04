package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdwardKenway.class, RenegadeFreighter.class, TalasScout.class,
        GrizzlyBears.class, Divination.class})
class EdwardKenwayTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure for each tapped Assassin, Pirate, and Vehicle you control")
    void createsTreasureForEachTappedQualifyingPermanent() {
        Permanent edward = addCreatureReady(player1, new EdwardKenway());
        edward.tap();
        Permanent pirate = addCreatureReady(player1, new TalasScout());
        pirate.tap();
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());
        vehicle.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Vehicle combat damage exiles the damaged player's top card face down with play permission")
    void vehicleCombatDamageExilesTopCardFaceDown() {
        addCreatureReady(player1, new EdwardKenway());
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());
        vehicle.setAnimatedUntilEndOfTurn(true);
        vehicle.setAnimatedPower(4);
        vehicle.setAnimatedToughness(3);
        vehicle.setAttacking(true);
        vehicle.setAttackTarget(player2.getId());

        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Combat damage from a non-Vehicle does not trigger the exile ability")
    void nonVehicleCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new EdwardKenway());
        Permanent attacker = addCreatureReady(player1, new TalasScout());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Exiled spells still require their normal mana colors")
    void exiledSpellRequiresNormalManaColors() {
        addCreatureReady(player1, new EdwardKenway());
        attackingVehicle(player1, player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        resolveCombat();
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Play permission survives the source leaving and permits casting with the correct mana")
    void canCastAfterEdwardLeaves() {
        Permanent edward = addCreatureReady(player1, new EdwardKenway());
        attackingVehicle(player1, player2);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        resolveCombat();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(edward);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("An opponent's Vehicle does not trigger Edward")
    void opposingVehicleDoesNotTrigger() {
        addCreatureReady(player1, new EdwardKenway());
        attackingVehicle(player2, player1);
        Card topCard = new Divination();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Treasure count is determined on resolution and excludes untapped and opposing permanents")
    void treasureCountUsesResolutionState() {
        Permanent edward = addCreatureReady(player1, new EdwardKenway());
        edward.tap();
        Permanent vehicle = addCreatureReady(player1, new RenegadeFreighter());
        addCreatureReady(player1, new TalasScout());
        addCreatureReady(player2, new TalasScout()).tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        edward.untap();
        vehicle.tap();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An empty damaged library produces no exiled card")
    void emptyLibraryDoesNotExile() {
        addCreatureReady(player1, new EdwardKenway());
        attackingVehicle(player1, player2);
        harness.setLibrary(player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    private Permanent attackingVehicle(Player controller, Player defender) {
        Permanent vehicle = addCreatureReady(controller, new RenegadeFreighter());
        vehicle.setAnimatedUntilEndOfTurn(true);
        vehicle.setAnimatedPower(4);
        vehicle.setAnimatedToughness(3);
        vehicle.setAttacking(true);
        vehicle.setAttackTarget(defender.getId());
        return vehicle;
    }
}
