package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WizardReplica;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfWinds.class, Forest.class, Island.class, GrizzlyBears.class, Shock.class, WizardReplica.class})
class MasterOfWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws two cards, then discards one")
    void enteringDrawsTwoThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new MasterOfWinds(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Master of Winds"));
    }

    @Test
    @DisplayName("Casting an instant offers the base power and toughness choice")
    void castingInstantOffersBasePowerAndToughnessChoice() {
        Permanent master = addMasterOfWinds();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("4/1", "1/4");
        harness.handleListChoice(player1, "4/1");

        assertThat(master.getEffectivePower()).isEqualTo(4);
        assertThat(master.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The base power and toughness choice wears off at end of turn")
    void basePowerAndToughnessChoiceWearsOffAtEndOfTurn() {
        Permanent master = addMasterOfWinds();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "4/1");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(master.getEffectivePower()).isEqualTo(1);
        assertThat(master.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a Wizard offers the base power and toughness choice")
    void castingWizardOffersBasePowerAndToughnessChoice() {
        Permanent master = addMasterOfWinds();
        harness.setHand(player1, List.of(new WizardReplica()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1/4");

        assertThat(master.getEffectivePower()).isEqualTo(1);
        assertThat(master.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting an unrelated creature does not trigger the ability")
    void castingUnrelatedCreatureDoesNotTrigger() {
        Permanent master = addMasterOfWinds();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(master.getEffectivePower()).isEqualTo(1);
        assertThat(master.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addMasterOfWinds() {
        return harness.addToBattlefieldAndReturn(player1, new MasterOfWinds());
    }
}
