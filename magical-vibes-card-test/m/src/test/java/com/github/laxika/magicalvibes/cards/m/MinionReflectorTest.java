package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.d.DeftDuelist;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinionReflector.class, CylianElf.class, Clone.class, DeftDuelist.class, ResoundingThunder.class})
class MinionReflectorTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering triggers the may-pay ability")
    void nontokenCreatureEnteringTriggersMayPay() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature spell, creature enters, trigger queued
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Paying {2} creates a hasty token copy scheduled to be sacrificed at end step")
    void payingCreatesHastyTokenCopySacrificedAtEndStep() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        long creatureCount = countPermanents(player1, "Cylian Elf");
        assertThat(creatureCount).isEqualTo(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cylian Elf") && p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(countPermanents(player1, "Cylian Elf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining does not create a token")
    void decliningDoesNotCreateToken() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        long creatureCount = countPermanents(player1, "Cylian Elf");
        assertThat(creatureCount).isEqualTo(1);
    }

    @Test
    @DisplayName("The created token copy is a token and does not trigger the ability again")
    void tokenCopyDoesNotRetrigger() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        // The token (a token creature) entering must NOT trigger Minion Reflector again.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("The copy uses last known information after the entering creature dies")
    void copiesCreatureThatLeftBeforeResolution() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Cylian Elf");

        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, original.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(original);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        Permanent token = findPermanent(player1, "Cylian Elf");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("A creature with shroud can be copied because the ability does not target")
    void copiesCreatureWithShroud() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new DeftDuelist(), "{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(countPermanents(player1, "Deft Duelist")).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's nontoken creature does not trigger the reflector")
    void opponentCreatureDoesNotTrigger() {
        addMinionReflectorReady(player1);
        harness.enterBattlefieldAndReturn(player2, new CylianElf());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting without enough mana does not create a token")
    void cannotCopyWithoutPayingTwoMana() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(countPermanents(player1, "Cylian Elf")).isEqualTo(1);
    }

    @Test
    @DisplayName("A copy of the token inherits its end-step sacrifice ability")
    void copyOfTokenAlsoHasToBeSacrificed() {
        addMinionReflectorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new CylianElf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent token = findPermanents(player1, "Cylian Elf").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, token.getId());
        Permanent copy = findPermanent(player2, "Cylian Elf");
        assertThat(copy.getCard().getKeywords()).contains(Keyword.HASTE);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(countPermanents(player1, "Cylian Elf")).isEqualTo(1);
    }

    private Permanent addMinionReflectorReady(Player player) {
        return addCreatureReady(player, new MinionReflector());
    }
}
