package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EagerFlameguide.class, GrizzlyBears.class, Shock.class, LightningBolt.class, MindRot.class, TimeWarp.class})
class EagerFlameguideTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB adds three mana restricted to creature spells")
    void etbAddsCreatureSpellOnlyMana() {
        harness.setHand(player1, List.of(new EagerFlameguide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(3);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its death exiles the top two cards and allows casting exiled creatures")
    void deathExilesTopTwoAndAllowsCreatureSpells() {
        Card exiledCreature = new GrizzlyBears();
        Card exiledNoncreature = new Shock();
        harness.setLibrary(player1, List.of(exiledCreature, exiledNoncreature));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(exiledCreature, exiledNoncreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledNoncreature.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB mana cannot pay the generic cost of a noncreature spell")
    void restrictedManaCannotCastNoncreatureSpell() {
        harness.setHand(player1, List.of(new EagerFlameguide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Death with only one library card exiles that card")
    void deathWithOneCardInLibrary() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creatures still require normal timing and mana payment")
    void exilePermissionDoesNotBypassTimingOrCosts() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Shock()));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.playerManaPools.get(player1.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("An opponent's extra turn does not shorten the exile permission")
    void permissionSurvivesOpponentsExtraTurn() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Shock(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);
        harness.setHand(player2, List.of(new TimeWarp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player2, 0, player2.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exile permission ends after the controller's next turn")
    void permissionExpiresAfterNextTurn() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Shock(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Death with an empty library resolves without exiling anything")
    void deathWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Eager Flameguide");
    }

    @Test
    @DisplayName("Both exiled creatures may be cast and the third library card stays put")
    void bothExiledCreaturesCanBeCast() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card unrelated = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, unrelated));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrelated);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, first.getId());
        resolveAllTriggers();
        harness.castFromExile(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled noncreature spells cannot be cast even with sufficient mana")
    void noncreaturePermissionIsNotGranted() {
        Card noncreature = new Shock();
        harness.setLibrary(player1, List.of(noncreature, new GrizzlyBears()));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EagerFlameguide());
        destroyWithBolt(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, noncreature.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(noncreature);
    }

    private void destroyWithBolt(Permanent target) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }
}
