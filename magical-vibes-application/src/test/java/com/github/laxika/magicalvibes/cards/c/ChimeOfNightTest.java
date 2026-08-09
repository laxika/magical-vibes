package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChimeOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("When put into a graveyard from the battlefield, it destroys a target nonblack creature")
    void destroysTargetNonblackCreature() {
        Permanent enchantedBlackCreature = addCreatureReady(player2, new ScatheZombies());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChimeOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantedBlackCreature.getId());
        harness.passBothPriorities();

        Permanent chime = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Chime of Night"))
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chime));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantedBlackCreature.getId()));
    }

    @Test
    @DisplayName("Does not target a black creature")
    void doesNotTargetBlackCreature() {
        Permanent blackCreature = addCreatureReady(player2, new ScatheZombies());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChimeOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        Permanent chime = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Chime of Night"))
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chime));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blackCreature.getId()));
    }
}
