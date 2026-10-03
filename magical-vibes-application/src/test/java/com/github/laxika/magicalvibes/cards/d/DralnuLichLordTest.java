package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DralnuLichLord.class, GrizzlyBears.class, Shock.class, SuddenSpoiling.class,
        ThinkTwice.class, Assassinate.class})
class DralnuLichLordTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to Dralnu is replaced by sacrificing permanents")
    void damageIsReplacedBySacrificingPermanents() {
        harness.addToBattlefield(player1, new DralnuLichLord());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent dralnu = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveInstant(player2, 0, dralnu.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dralnu, Lich Lord");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage to Dralnu is replaced by sacrificing permanents")
    void combatDamageIsReplacedBySacrificingPermanents() {
        Permanent dralnu = addCreatureReady(player2, new DralnuLichLord());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        dralnu.setBlocking(true);
        dralnu.addBlockingTarget(0);

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Dralnu, Lich Lord");
    }

    @Test
    @DisplayName("The tap ability grants flashback to a targeted instant or sorcery")
    void tapAbilityGrantsFlashback() {
        addCreatureReady(player1, new DralnuLichLord());
        Shock shock = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void controllerChoosesOtherPermanentsToSacrificeAndDralnuTakesNoDamage() {
        Permanent dralnu = harness.addToBattlefieldAndReturn(player1, new DralnuLichLord());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, dralnu.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dralnu);
        assertThat(dralnu.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
    }

    @Test
    void losingAbilitiesDisablesDamageReplacement() {
        Permanent dralnu = harness.addToBattlefieldAndReturn(player1, new DralnuLichLord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new SuddenSpoiling(), new Shock()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, dralnu.getId());

        harness.assertInGraveyard(player1, "Dralnu, Lich Lord");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void grantsCheaperFlashbackEvenWhenCardAlreadyHasFlashback() {
        addCreatureReady(player1, new DralnuLichLord());
        ThinkTwice spell = new ThinkTwice();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, spell.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void grantsFlashbackToSorceryForItsManaCost() {
        Permanent dralnu = addCreatureReady(player1, new DralnuLichLord());
        Assassinate spell = new Assassinate();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setTapped(true);
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, spell.getId(), Zone.GRAVEYARD);
        assertThat(dralnu.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.castAndResolveFlashback(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void cannotTargetCreatureOrOpponentsGraveyard() {
        Permanent dralnu = addCreatureReady(player1, new DralnuLichLord());
        GrizzlyBears creature = new GrizzlyBears();
        Shock opponentsSpell = new Shock();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opponentsSpell));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentsSpell.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dralnu.isTapped()).isFalse();
    }

    @Test
    void grantedFlashbackExpiresAtEndOfTurn() {
        addCreatureReady(player1, new DralnuLichLord());
        Shock spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, spell.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
    }
}
