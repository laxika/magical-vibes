package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyggRiverGuide.class, GrizzlyBears.class, SilvergillAdept.class, Island.class})
class SyggRiverGuideTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W}: a targeted Merfolk you control gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        Permanent sygg = harness.addToBattlefieldAndReturn(player1, new SyggRiverGuide());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID syggId = sygg.getId();
        harness.activateAbility(player1, 0, null, syggId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null).isTrue();
        harness.handleListChoice(player1, "RED");

        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        Permanent sygg = harness.addToBattlefieldAndReturn(player1, new SyggRiverGuide());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID syggId = sygg.getId();
        harness.activateAbility(player1, 0, null, syggId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("The ability cannot target a non-Merfolk you control")
    void cannotTargetNonMerfolk() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantsProtectionToAnotherMerfolkWithoutTappingSygg() {
        Permanent sygg = harness.addToBattlefieldAndReturn(player1, new SyggRiverGuide());
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SilvergillAdept());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, adept.getId());
        assertThat(sygg.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(adept.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    void cannotTargetOpponentsMerfolk() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        Permanent adept = harness.addToBattlefieldAndReturn(player2, new SilvergillAdept());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, adept.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void genericManaCannotPayTheWhiteRequirement() {
        Permanent sygg = harness.addToBattlefieldAndReturn(player1, new SyggRiverGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sygg.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedActivationsCanGrantMultipleColors() {
        Permanent sygg = harness.addToBattlefieldAndReturn(player1, new SyggRiverGuide());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, sygg.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 0, null, sygg.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLACK);
        assertThat(sygg.isTapped()).isFalse();
    }

    @Test
    void protectionFromWhitePreventsFurtherTargetingBySygg() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SilvergillAdept());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, adept.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, adept.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pendingAbilityDoesNotResolveAfterTargetGainsProtectionFromWhite() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SilvergillAdept());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, adept.getId());
        harness.activateAbility(player1, 0, null, adept.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(adept.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.WHITE);
    }

    @Test
    void abilityResolvesAfterSyggLeavesBattlefield() {
        Permanent sygg = harness.addToBattlefieldAndReturn(player1, new SyggRiverGuide());
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SilvergillAdept());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, adept.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sygg);
        gd.playerGraveyards.get(player1.getId()).add(sygg.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(adept.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.RED);
    }

    @Test
    void islandwalkPreventsBlockingWhenDefenderControlsIsland() {
        Permanent sygg = addCreatureReady(player1, new SyggRiverGuide());
        sygg.setAttacking(true);
        addCreatureReady(player2, new SilvergillAdept());
        harness.addToBattlefield(player2, new Island());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void islandwalkChecksDefendersIslandsRatherThanAttackers() {
        Permanent sygg = addCreatureReady(player1, new SyggRiverGuide());
        sygg.setAttacking(true);
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new SilvergillAdept());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
