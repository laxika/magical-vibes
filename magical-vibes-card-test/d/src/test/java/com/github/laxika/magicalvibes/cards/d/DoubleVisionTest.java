package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToSlag;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoubleVision.class, GrizzlyBears.class, LightningBolt.class, Cancel.class,
        OnakkeOgre.class, Revitalize.class, Shock.class, TurnToSlag.class, VolcanicGeyser.class})
class DoubleVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the first instant or sorcery after ignoring an earlier creature spell")
    void copiesFirstInstantOrSorceryEachTurn() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger again for a second instant or sorcery in the same turn")
    void onlyTriggersForTheFirstInstantOrSorceryEachTurn() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    void doesNotTriggerIfAnInstantWasCastBeforeItEntered() {
        harness.setHand(player1, List.of(new Shock(), new DoubleVision(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void decliningNewTargetsStillResolvesTheMandatoryCopy() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void canChooseANewTargetWithoutChangingTheOriginal() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void copiesSpellsWithoutTargets() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void copiesTheChosenXValue() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void copiesTheFirstSorceryAndCanRetargetIt() {
        harness.addToBattlefield(player1, new DoubleVision());
        var first = harness.addToBattlefieldAndReturn(player2, new OnakkeOgre());
        var second = harness.addToBattlefieldAndReturn(player2, new OnakkeOgre());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void stillCopiesTheSpellAfterTheOriginalIsCountered() {
        harness.addToBattlefield(player1, new DoubleVision());
        var shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void opponentsSpellDoesNotTriggerOrConsumeTheControllersFirstSpell() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    void triggersAgainOnTheOpponentsTurn() {
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }
}
