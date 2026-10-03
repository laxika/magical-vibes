package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamspoilerWitches.class, WoodlandChangeling.class, GoldmeadowHarrier.class, Tarfire.class})
class DreamspoilerWitchesTest extends BaseCardTest {

    /** Puts player1 on defense during player2's turn so player1 may cast an instant. */
    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting during an opponent's turn requires a target before the optional choice")
    void triggersDuringOpponentTurn() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting gives target creature -1/-1 until end of turn")
    void acceptDebuffsTarget() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("-1/-1 kills a 1/1 creature")
    void debuffKillsOneOneCreature() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        UUID elvesId = harness.addToBattlefieldAndReturn(player2, new GoldmeadowHarrier()).getId();

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Harrier");
        harness.assertInGraveyard(player2, "Goldmeadow Harrier");
    }

    @Test
    @DisplayName("The may ability can target any creature, but affects only the chosen one")
    void targetsAnyCreatureAndOnlyDebuffsChosenCreature() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        UUID ownBearsId = harness.getPermanentId(player1, "Woodland Changeling");
        UUID opponentBearsId = harness.getPermanentId(player2, "Woodland Changeling");

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(ownBearsId, opponentBearsId);

        harness.handlePermanentChosen(player1, ownBearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Woodland Changeling"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Woodland Changeling"))).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Woodland Changeling"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Woodland Changeling"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining leaves the target unchanged")
    void declineLeavesTarget() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        harness.addToBattlefield(player2, new WoodlandChangeling());

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Woodland Changeling"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The -1/-1 lasts only until end of turn")
    void debuffExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player2, "Woodland Changeling");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting on your own turn does not trigger")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting a spell does not trigger it")
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        enterOpponentTurn();
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The target is chosen before responses and the optional debuff resolves before the spell")
    void choosesWhetherToDebuffOnlyWhenTriggerResolves() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A target removed in response makes the trigger fail to resolve")
    void removedTargetDoesNotAllowRetargeting() {
        harness.addToBattlefield(player1, new DreamspoilerWitches());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player2, "Woodland Changeling");

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Dreamspoiler Witches"))).isEqualTo(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }
}
