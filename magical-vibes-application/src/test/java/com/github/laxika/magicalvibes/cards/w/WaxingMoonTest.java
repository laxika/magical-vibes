package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaxingMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Waxing Moon transforms a targeted Werewolf and grants trample to your creatures")
    void transformsWerewolfAndGrantsTrample() {
        Permanent werewolf = addReadyCreature(player1, new WolfbittenCaptive());
        Permanent otherCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, werewolf.getId());
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isTrue();
        assertThat(werewolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(otherCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Waxing Moon may be cast without transforming a Werewolf")
    void mayChooseNoTarget() {
        Permanent werewolf = addReadyCreature(player1, new WolfbittenCaptive());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isFalse();
        assertThat(werewolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Waxing Moon rejects a target that is not a Werewolf you control")
    void rejectsIllegalTarget() {
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Werewolf you control");
    }

    @Test
    @DisplayName("Waxing Moon's trample effect wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaxingMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
