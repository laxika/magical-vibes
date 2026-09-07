package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EddymurkCrab.class, GrizzlyBears.class, LavaAxe.class, Shock.class})
class EddymurkCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each instant or sorcery card in its controller's graveyard")
    void costReductionCountsInstantAndSorceryCards() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new EddymurkCrab()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Enters untapped during its controller's turn")
    void entersUntappedOnControllerTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithTarget(player1, List.of(target.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent crab = findCrab(player1);
        assertThat(crab.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped during an opponent's turn and taps up to two creatures")
    void entersTappedAndTapsTwoCreaturesOnOpponentTurn() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        castWithTarget(player1, List.of(firstTarget.getId(), secondTarget.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent crab = findCrab(player1);
        assertThat(crab.isTapped()).isTrue();
        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    private void castWithTarget(Player caster, List<UUID> targets) {
        harness.setHand(caster, List.of(new EddymurkCrab()));
        harness.addMana(caster, ManaColor.BLUE, 7);
        harness.castCreature(caster, 0, targets);
    }

    private Permanent findCrab(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EddymurkCrab)
                .findFirst()
                .orElseThrow();
    }
}
