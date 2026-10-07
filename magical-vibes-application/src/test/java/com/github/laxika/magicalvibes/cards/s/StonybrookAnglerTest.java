package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.w.WanderersTwig;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonybrookAngler.class, AxegrinderGiant.class, WanderersTwig.class})
class StonybrookAnglerTest extends BaseCardTest {


    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsOnStack() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(angler.getId());
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Stonybrook Angler")
    void activatingTapsAngler() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(angler.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Taps an untapped creature")
    void tapsUntappedCreature() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        target.tap();
        addAnglerMana(player1);

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("May decline to tap or untap the target creature")
    void mayDeclineTapOrUntap() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }


    @Test
    @DisplayName("Can tap own untapped creature")
    void canTapOwnCreature() {
        addReadyAngler(player1);
        Permanent ownCreature = addCreatureReady(player1, new AxegrinderGiant());
        addAnglerMana(player1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyAngler(player1);
        Permanent artifactPerm = harness.addToBattlefieldAndReturn(player2, new WanderersTwig());
        addAnglerMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent angler = addReadyAngler(player1);
        angler.tap();
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new StonybrookAngler());
        // summoningSick is true by default
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }


    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        addAnglerMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }


    @Test
    @DisplayName("Can decline to untap itself after paying its tap cost")
    void canDeclineToUntapItself() {
        Permanent angler = addReadyAngler(player1);
        addAnglerMana(player1);

        harness.activateAbility(player1, 0, null, angler.getId());
        assertThat(angler.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(angler.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(angler.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyAngler(Player player) {
        return addCreatureReady(player, new StonybrookAngler());
    }

    private void addAnglerMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
