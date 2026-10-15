package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForlornFlats.class})
class ForlornFlatsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and deals 1 damage to a target opponent")
    void entersTappedAndDamagesTargetOpponent() {
        harness.setHand(player1, List.of(new ForlornFlats()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping produces one white mana")
    void tapsForWhiteMana() {
        harness.addToBattlefield(player1, new ForlornFlats());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping produces one black mana")
    void tapsForBlackMana() {
        harness.addToBattlefield(player1, new ForlornFlats());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry trigger still deals damage after the land leaves the battlefield")
    void entryTriggerResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new ForlornFlats()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Forlorn Flats");
        harness.assertInGraveyard(player1, "Forlorn Flats");
    }
}
