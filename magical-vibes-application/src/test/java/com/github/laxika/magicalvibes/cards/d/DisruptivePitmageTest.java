package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptivePitmage.class, GlorySeeker.class})
class DisruptivePitmageTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerCannotPay() {
        addCreatureReady(player2, new DisruptivePitmage());
        GlorySeeker spell = new GlorySeeker();
        harness.castFromHand(player1, spell, "{1}{W}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotOnBattlefield(player1, "Glory Seeker");
    }

    @Test
    void spellControllerMayPayOneMana() {
        addCreatureReady(player2, new DisruptivePitmage());
        GlorySeeker spell = new GlorySeeker();
        harness.castFromHand(player1, spell, "{1}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glory Seeker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void spellControllerMayDeclineToPayOneMana() {
        addCreatureReady(player2, new DisruptivePitmage());
        GlorySeeker spell = new GlorySeeker();
        harness.castFromHand(player1, spell, "{1}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        assertThat(gameLogContains("declines to pay {1}")).isTrue();
    }

    @Test
    void cannotTargetPermanent() {
        addCreatureReady(player2, new DisruptivePitmage());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent pitmage = addCreatureReady(player2, new DisruptivePitmage());
        GlorySeeker spell = new GlorySeeker();
        harness.castFromHand(player1, spell, "{1}{W}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());

        assertThat(pitmage.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeMorphedFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new DisruptivePitmage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent pitmage = findPermanent(player1, "Disruptive Pitmage");
        assertThat(pitmage.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pitmage));
        harness.passBothPriorities();

        assertThat(pitmage.isFaceDown()).isFalse();
    }
}
