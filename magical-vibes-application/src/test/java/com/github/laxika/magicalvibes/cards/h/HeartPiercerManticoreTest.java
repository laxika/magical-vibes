package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({HeartPiercerManticore.class, GrizzlyBears.class, HillGiant.class})
class HeartPiercerManticoreTest extends BaseCardTest {

    /** Casts Heart-Piercer Manticore and advances to its "sacrifice another creature?" prompt. */
    private void castManticoreToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> enters, ETB may on stack
        harness.passBothPriorities(); // resolve ETB may -> prompt
    }

    @Test
    @DisplayName("Sacrificing a creature deals damage equal to its power to a target player")
    void sacrificeDealsPowerDamageToPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 to sacrifice
        int lifeBefore = gd.getLife(player2.getId());

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // 2 damage (the sacrificed Grizzly Bears' power), not 4 (the Manticore's power).
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage equals the sacrificed creature's power, not the Manticore's")
    void damageTracksSacrificedCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());  // 2/2 to sacrifice
        harness.addToBattlefield(player2, new HillGiant());     // 3/3 target

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        // The 3/3 survives 2 damage; it would have died to the Manticore's own power (4).
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Enough power kills the targeted creature")
    void lethalDamageDestroysTargetCreature() {
        harness.addToBattlefield(player1, new HillGiant());     // 3/3 to sacrifice
        harness.addToBattlefield(player2, new GrizzlyBears());  // 2/2 target

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Hill Giant"));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Declining the sacrifice deals no damage and keeps the creature")
    void decliningSacrificeDealsNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no other creature to sacrifice, the trigger deals no damage")
    void noOtherCreatureDealsNoDamage() {
        int lifeBefore = gd.getLife(player2.getId());

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.RED, 6);
    }

    @Test
    void embalmExilesAsCostAndCreatesModifiedTokenWithEnterAbility() {
        setUpEmbalm();
        harness.addToBattlefield(player1, new HeartPiercerManticore());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Heart-Piercer Manticore");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Heart-Piercer Manticore"));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.MANTICORE);
        assertThat(token.getCard().getManaCost()).isEmpty();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent original = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getCard().isToken()).findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, original.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Heart-Piercer Manticore");
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(token);
    }

    @Test
    void embalmCannotBeActivatedOnOpponentsTurn() {
        setUpEmbalm();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Heart-Piercer Manticore");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void embalmCannotBeActivatedDuringCombat() {
        setUpEmbalm();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Heart-Piercer Manticore");
    }

    @Test
    void embalmRequiresEmptyStack() {
        setUpEmbalm();
        harness.setHand(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Heart-Piercer Manticore");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void embalmRequiresSixMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Heart-Piercer Manticore");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void embalmRequiresRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Heart-Piercer Manticore");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
