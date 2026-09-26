package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoratamiSavant.class, Island.class, Forest.class, DevotedRetainer.class, ReachThroughMists.class})
class SoratamiSavantTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell when its controller cannot pay {3}")
    void countersWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player2, retainer, "{W}");

        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, retainer.getId());

        harness.assertInHand(player1, "Island");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Devoted Retainer");
        harness.assertNotOnBattlefield(player2, "Devoted Retainer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell resolves when its controller pays {3}")
    void spellSurvivesWhenControllerPays() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player2, retainer, "{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, retainer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player2, "Devoted Retainer");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Devoted Retainer");
    }

    @Test
    @DisplayName("Spell is countered when its controller declines to pay")
    void spellCounteredWhenControllerDeclines() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player2, retainer, "{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, retainer.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Devoted Retainer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player2, retainer, "{W}");

        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Savant"), 0, retainer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's land to pay the return cost")
    void cannotActivateWithOnlyOpponentsLand() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player2, retainer, "{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Savant"), 0, retainer.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesLandWhenSeveralAreAvailable() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        DevotedRetainer retainer = new DevotedRetainer();
        harness.castFromHand(player2, retainer, "{W}");
        harness.passPriority(player2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, retainer.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, forest.getId());

        assertThat(gd.stack).hasSize(2);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Devoted Retainer");
    }

    @Test
    @DisplayName("Cannot target a permanent with the activated ability")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new DevotedRetainer());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Savant"), 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a noncreature spell")
    void countersNonCreatureSpell() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        ReachThroughMists spell = new ReachThroughMists();
        harness.castFromHand(player2, spell, "{U}");
        harness.passPriority(player2);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, spell.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Reach Through Mists");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target and counter a spell controlled by its controller")
    void canTargetItsOwnSpell() {
        harness.addToBattlefield(player1, new SoratamiSavant());
        harness.addToBattlefield(player1, new Island());
        harness.forceActivePlayer(player1);

        ReachThroughMists spell = new ReachThroughMists();
        harness.castFromHand(player1, spell, "{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Savant"), 0, spell.getId());

        harness.assertInHand(player1, "Island");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reach Through Mists");
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
