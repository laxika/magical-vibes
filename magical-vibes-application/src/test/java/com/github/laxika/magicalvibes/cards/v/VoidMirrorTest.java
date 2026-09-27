package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VoidMirror.class, MindStone.class})
class VoidMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell cast using only colorless mana")
    void countersSpellCastUsingOnlyColorlessMana() {
        harness.addToBattlefield(player1, new VoidMirror());
        castMindStone(player2, ManaColor.COLORLESS, 2);

        harness.assertInGraveyard(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Does not counter a spell when colored mana was spent")
    void doesNotCounterSpellWhenColoredManaWasSpent() {
        harness.addToBattlefield(player1, new VoidMirror());
        castMindStone(player2, ManaColor.BLUE, 2);

        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    private void castMindStone(Player player, ManaColor manaColor, int amount) {
        harness.setHand(player, List.of(new MindStone()));
        harness.addMana(player, manaColor, amount);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player, 0);
        harness.passBothPriorities();
    }
}
