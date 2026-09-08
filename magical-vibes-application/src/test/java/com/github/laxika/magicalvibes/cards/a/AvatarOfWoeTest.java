package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarOfWoeTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast for {B}{B} when there are ten creature cards across all graveyards")
    void costsLessWithTenCreatureCardsAcrossGraveyards() {
        harness.setGraveyard(player1, IntStream.range(0, 9).<Card>mapToObj(i -> new GrizzlyBears()).toList());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AvatarOfWoe()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce its cost for noncreature cards in graveyards")
    void doesNotCountNoncreatureCards() {
        harness.setGraveyard(player1, IntStream.range(0, 9).<Card>mapToObj(i -> new GrizzlyBears()).toList());
        harness.setGraveyard(player2, List.of(new HolyDay()));
        harness.setHand(player1, List.of(new AvatarOfWoe()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tap ability destroys a target creature without allowing regeneration")
    void destroysTargetCreatureWithoutRegeneration() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfWoe());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setRegenerationShield(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avatar);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
