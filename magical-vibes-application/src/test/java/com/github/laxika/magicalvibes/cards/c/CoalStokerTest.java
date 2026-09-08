package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalStoker.class, Zombify.class})
class CoalStokerTest extends BaseCardTest {

    @Test
    @DisplayName("When Coal Stoker is cast from hand, its controller gets three red mana")
    void castFromHandAddsThreeRedMana() {
        harness.setHand(player1, List.of(new CoalStoker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Coal Stoker");
    }

    @Test
    @DisplayName("Returning Coal Stoker from a graveyard does not add mana")
    void returningFromGraveyardDoesNotAddMana() {
        CoalStoker stoker = new CoalStoker();
        harness.setGraveyard(player1, List.of(stoker));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, stoker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertOnBattlefield(player1, "Coal Stoker");
    }
}
