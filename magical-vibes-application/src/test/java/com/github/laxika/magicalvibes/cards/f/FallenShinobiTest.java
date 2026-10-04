package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaticSinkhole;
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

@CardUsed({FallenShinobi.class, Forest.class, GrizzlyBears.class, MagmaticSinkhole.class})
class FallenShinobiTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top two cards of the damaged player's library")
    void combatDamageExilesTopTwoCards() {
        addAttackingShinobi();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card remainder = new Forest();
        harness.setLibrary(player2, List.of(first, second, remainder));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainder);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("The exiled land and creature can be played without paying their mana costs")
    void playsExiledLandAndCreatureForFree() {
        addAttackingShinobi();
        Forest exiledLand = new Forest();
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledLand, exiledCreature));

        resolveCombatAndTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, exiledLand.getId());
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledLand.getId())).isNull();
        assertThat(gd.findExiledCard(exiledCreature.getId())).isNull();
    }

    @Test
    @DisplayName("Ninjutsu puts Fallen Shinobi onto the battlefield tapped and attacking")
    void ninjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FallenShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.activateHandAbility(player1, 0, attacker.getId());
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent shinobi = findPermanent(player1, "Fallen Shinobi");
        assertThat(shinobi.isTapped()).isTrue();
        assertThat(shinobi.isAttacking()).isTrue();
        assertThat(shinobi.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A library with only one card exiles that card without requiring a second")
    void exilesOnlyRemainingCard() {
        addAttackingShinobi();
        FallenShinobi remaining = new FallenShinobi();
        harness.setLibrary(player2, List.of(remaining));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, remaining.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Fallen Shinobi")).hasSize(2);
    }

    @Test
    @DisplayName("An empty library does not prevent the combat damage trigger from resolving")
    void emptyLibrary() {
        addAttackingShinobi();
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Exiled creatures still require main-phase timing")
    void freeCastingDoesNotBypassTiming() {
        addAttackingShinobi();
        FallenShinobi exiled = new FallenShinobi();
        harness.setLibrary(player2, List.of(exiled));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiled);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Fallen Shinobi")).hasSize(2);
    }

    @Test
    @DisplayName("The owner cannot use Fallen Shinobi's play permission")
    void onlyTriggerControllerCanPlayCards() {
        addAttackingShinobi();
        FallenShinobi exiled = new FallenShinobi();
        harness.setLibrary(player2, List.of(exiled));
        resolveCombatAndTrigger();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiled);
    }

    @Test
    @DisplayName("Unplayed cards remain exiled after the play permission expires")
    void permissionExpiresAtEndOfTurn() {
        addAttackingShinobi();
        FallenShinobi exiled = new FallenShinobi();
        harness.setLibrary(player2, List.of(exiled));
        resolveCombatAndTrigger();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiled);
    }

    @Test
    @DisplayName("The permission does not allow a second land play")
    void normalLandPlayLimitApplies() {
        addAttackingShinobi();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("An exiled spell with delve can be cast for free without exiling graveyard cards")
    void castsDelveSpellWithoutPayingManaCost() {
        Permanent shinobi = addAttackingShinobi();
        MagmaticSinkhole exiled = new MagmaticSinkhole();
        harness.setLibrary(player2, List.of(exiled));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, exiled.getId(), shinobi.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fallen Shinobi");
        harness.assertInGraveyard(player2, "Magmatic Sinkhole");
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    private Permanent addAttackingShinobi() {
        Permanent shinobi = addCreatureReady(player1, new FallenShinobi());
        shinobi.setAttacking(true);
        shinobi.setAttackTarget(player2.getId());
        return shinobi;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
