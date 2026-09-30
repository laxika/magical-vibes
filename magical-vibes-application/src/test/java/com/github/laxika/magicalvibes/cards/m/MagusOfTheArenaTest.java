package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheArena.class, GiantDustwasp.class, MireBoa.class})
class MagusOfTheArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent chooses the second creature, which is tapped and fights the first")
    void opponentChoosesSecondCreatureAndItFights() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new MireBoa());
        Permanent opposingDustwasp = addCreatureReady(player2, new GiantDustwasp());
        Permanent opposingBoa = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(opposingDustwasp.getId(), opposingBoa.getId());

        harness.handlePermanentChosen(player2, opposingDustwasp.getId());
        harness.passBothPriorities();

        assertThat(magus.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mire Boa");
        assertThat(opposingDustwasp.isTapped()).isTrue();
        assertThat(opposingDustwasp.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingBoa.isTapped()).isFalse();
        assertThat(opposingBoa.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Taps the remaining target but does not fight when one target is gone")
    void tapsRemainingTargetWhenOpponentTargetIsGone() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new GiantDustwasp());
        Permanent boa = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());
        harness.handlePermanentChosen(player2, boa.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(magus.isTapped()).isTrue();
        assertThat(fighter.isTapped()).isTrue();
        assertThat(fighter.getMarkedDamage()).isZero();
    }
}
