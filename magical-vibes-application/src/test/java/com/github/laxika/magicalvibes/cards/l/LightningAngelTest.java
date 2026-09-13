package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BloodfireDwarf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningAngel.class, BloodfireDwarf.class})
class LightningAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new LightningAngel());
        addCreatureReady(player2, new BloodfireDwarf());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Vigilance keeps it untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent angel = addCreatureReady(player1, new LightningAngel());

        declareAttackers(player1, List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste lets it attack the turn it enters the battlefield")
    void hasteLetsItAttackImmediately() {
        harness.castFromHand(player1, new LightningAngel(), "{1}{U}{R}{W}");
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        Permanent angel = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(angel.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }
}
