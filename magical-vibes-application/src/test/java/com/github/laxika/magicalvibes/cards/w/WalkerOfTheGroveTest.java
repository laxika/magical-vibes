package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WalkerOfTheGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Leaving the battlefield creates a 4/4 green Elemental token")
    void leavesBattlefieldCreatesToken() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new WalkerOfTheGrove());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, walker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve LTB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, then LTB creates a 4/4 Elemental token")
    void evokeSacrificesThenCreatesToken() {
        harness.setHand(player1, List.of(new WalkerOfTheGrove()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB (evoke sacrifice) -> LTB trigger
        harness.passBothPriorities(); // resolve LTB trigger -> token

        // Walker itself was sacrificed as it entered.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Walker of the Grove"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walker of the Grove"));

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Elemental");
    }
}
