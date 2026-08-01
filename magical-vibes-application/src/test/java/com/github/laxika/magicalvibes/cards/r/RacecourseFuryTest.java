package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

class RacecourseFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land taps to give target creature haste")
    void enchantedLandGrantsHaste() {
        Permanent mountain = attachFury(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(mountain.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at cleanup")
    void hasteExpiresAtCleanup() {
        attachFury(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a non-land permanent")
    void cannotEnchantCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RacecourseFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enchant a land an opponent controls")
    void canEnchantOpponentLand() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent opponentMountain = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new RacecourseFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentMountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> opponentMountain.getId().equals(p.getAttachedTo()));
    }

    private Permanent attachFury(Player player) {
        harness.addToBattlefield(player, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player.getId()).getFirst();
        Permanent aura = new Permanent(new RacecourseFury());
        aura.setAttachedTo(mountain.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return mountain;
    }
}
