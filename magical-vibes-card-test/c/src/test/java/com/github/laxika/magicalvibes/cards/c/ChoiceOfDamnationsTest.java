package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Deathgreeter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChoiceOfDamnations.class, GrizzlyBears.class, JayemdaeTome.class, Swamp.class,
        Deathgreeter.class})
class ChoiceOfDamnationsTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent chooses a number before the caster chooses the outcome")
    void targetChoosesNumberBeforeCasterChoosesOutcome() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        cast();

        PendingInteraction.XValueChoice numberChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(numberChoice).isNotNull();
        assertThat(numberChoice.playerId()).isEqualTo(player2.getId());

        harness.handleXValueChosen(player2, 4);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    @DisplayName("Declining life loss makes the target sacrifice all but the chosen number")
    void decliningLifeLossSacrificesAllButChosenNumber() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        Permanent tome = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());

        cast();
        harness.handleXValueChosen(player2, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId(), swamp.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertOnBattlefield(player2, "Jayemdae Tome");
        assertThat(tome).isIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Choosing more than the target's permanents sacrifices nothing")
    void choosingMoreThanPermanentsSacrificesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();
        harness.handleXValueChosen(player2, 2);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing zero and declining life loss sacrifices every permanent")
    void choosingZeroSacrificesEveryPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new JayemdaeTome());

        cast();
        harness.handleXValueChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertNotOnBattlefield(player2, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new ChoiceOfDamnations()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Accepting zero life loss keeps every permanent")
    void acceptingZeroLifeLossKeepsEveryPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        cast();
        harness.handleXValueChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Swamp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing the permanent count and declining life loss sacrifices nothing")
    void choosingExactlyPermanentCountSacrificesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        cast();
        harness.handleXValueChosen(player2, 2);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Swamp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent without permanents can still choose a number and lose life")
    void opponentWithoutPermanentsCanLoseLife() {
        cast();
        harness.handleXValueChosen(player2, 7);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing zero sacrifices creatures simultaneously so each sees the other's death")
    void choosingZeroSacrificesCreaturesSimultaneously() {
        harness.addToBattlefield(player2, new Deathgreeter());
        harness.addToBattlefield(player2, new Deathgreeter());

        cast();
        harness.handleXValueChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, false);

        acceptBothDeathgreeterTriggers();

        harness.assertNotOnBattlefield(player2, "Deathgreeter");
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Selected creatures are sacrificed simultaneously while the unselected land stays")
    void selectedCreaturesAreSacrificedSimultaneously() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Deathgreeter());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Deathgreeter());
        harness.addToBattlefield(player2, new Swamp());

        cast();
        harness.handleXValueChosen(player2, 1);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        acceptBothDeathgreeterTriggers();

        harness.assertNotOnBattlefield(player2, "Deathgreeter");
        harness.assertOnBattlefield(player2, "Swamp");
        harness.assertLife(player2, 22);
    }

    private void acceptBothDeathgreeterTriggers() {
        for (int i = 0; i < 2; i++) {
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) == null) {
                harness.passBothPriorities();
            }
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                    .isNotNull();
            harness.handleMayAbilityChosen(player2, true);
        }
    }

    private void cast() {
        harness.setHand(player1, List.of(new ChoiceOfDamnations()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
