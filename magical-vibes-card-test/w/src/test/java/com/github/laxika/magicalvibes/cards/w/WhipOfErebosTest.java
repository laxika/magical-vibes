package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WhipOfErebos.class, GrizzlyBears.class, LightningBolt.class, VoyagesEnd.class})
class WhipOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have lifelink")
    void grantsLifelinkToControlledCreatures() {
        harness.addToBattlefield(player1, new WhipOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Reanimates a target creature with haste")
    void reanimatesCreatureWithHaste() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(player1, whip), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent reanimated = findPermanent(player1, "Grizzly Bears");
        assertThat(reanimated.getCard().getId()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, reanimated, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Reanimated creature is exiled at the beginning of the next end step")
    void exilesReanimatedCreatureAtNextEndStep() {
        reanimateCreature();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Reanimated creature is exiled instead of going to the graveyard")
    void exilesReanimatedCreatureIfItWouldLeaveBattlefield() {
        reanimateCreature();
        Permanent reanimated = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, reanimated.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(reanimated.getCard().getId()));
    }

    @Test
    @DisplayName("The ability cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent whip = addReadyWhip();
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, whip), 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, whip),
                0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, whip),
                0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void exilesInsteadOfReturningToHand() {
        reanimateCreature();
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new VoyagesEnd()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(creature.getCard().getId()));
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.addToBattlefield(player1, new WhipOfErebos());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    void delayedExileTriggerHasWhipAsItsSource() {
        reanimateCreature();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(WhipOfErebos.class);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, whip),
                0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, whip),
                0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void delayedExileStillHappensAfterWhipLeavesBattlefield() {
        reanimateCreature();
        Permanent whip = findPermanent(player1, "Whip of Erebos");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, whip));
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(creature.getCard().getId()));
    }

    private void reanimateCreature() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(player1, whip), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }

    private Permanent addReadyWhip() {
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new WhipOfErebos());
        whip.setSummoningSick(false);
        return whip;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
