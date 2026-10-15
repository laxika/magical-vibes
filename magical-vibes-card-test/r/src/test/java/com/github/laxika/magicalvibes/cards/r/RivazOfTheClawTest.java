package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticInfantry;
import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RivazOfTheClaw.class, DragonWhelp.class, ElectrostaticInfantry.class, LightningStrike.class, EssenceScatter.class})
class RivazOfTheClawTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Rivaz adds two independently chosen Dragon creature mana")
    void addsTwoDragonCreatureMana() {
        Permanent rivaz = addCreatureReady(player1, new RivazOfTheClaw());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(rivaz.isTapped()).isTrue();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED))
                .isEqualTo(1);
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Rivaz's mana can cast a Dragon creature but not another creature")
    void manaIsRestrictedToDragonCreatures() {
        addCreatureReady(player1, new RivazOfTheClaw());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.setHand(player1, List.of(new ElectrostaticInfantry()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Rivaz permits only one Dragon creature cast from the graveyard each turn")
    void castsOneDragonFromGraveyardPerTurn() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp first = new DragonWhelp();
        DragonWhelp second = new DragonWhelp();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 8);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Dragon Whelp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Dragon cast from the graveyard is exiled when it dies")
    void exilesDragonWhenItDies() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        Permanent dragonPermanent = findPermanent(player1, "Dragon Whelp");
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, dragonPermanent.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Dragon Whelp");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Dragon Whelp");
    }

    @Test
    @DisplayName("Dragon mana cannot pay for a noncreature spell")
    void cannotSpendDragonManaOnInstant() {
        addCreatureReady(player1, new RivazOfTheClaw());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(new LightningStrike()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Rivaz does not permit non-Dragon creatures from the graveyard")
    void cannotCastNonDragonFromGraveyard() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        ElectrostaticInfantry infantry = new ElectrostaticInfantry();
        harness.setGraveyard(player1, List.of(infantry));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(infantry);
    }

    @Test
    @DisplayName("Rivaz does not let a Dragon be cast outside normal creature timing")
    void cannotCastDragonDuringCombat() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("A Dragon cast from hand is not exiled when it dies")
    void doesNotExileDragonCastFromHand() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp dragon = new DragonWhelp();
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Dragon Whelp");
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("A countered graveyard Dragon is not exiled and still consumes the casting permission")
    void counteredDragonStaysInGraveyardAndUsesPermission() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EssenceScatter()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.addMana(player2, ManaColor.BLUE, 2);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dragon.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(dragon);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted death ability survives the end of the turn")
    void exilesDragonThatDiesOnLaterTurn() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        Permanent permanent = findPermanent(player1, "Dragon Whelp");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, permanent.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dragon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("Rivaz allows another graveyard Dragon on the controller's next turn")
    void permissionResetsOnNextTurn() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp first = new DragonWhelp();
        DragonWhelp second = new DragonWhelp();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.clearPriorityPassed();
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dragon Whelp")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("Removing Rivaz in response does not stop its death-ability grant")
    void grantsDeathAbilityEvenIfRivazLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        Permanent rivaz = findPermanent(player1, "Rivaz of the Claw");
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 2);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, rivaz.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Rivaz of the Claw");

        Permanent permanent = findPermanent(player1, "Dragon Whelp");
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dragon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dragon);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
