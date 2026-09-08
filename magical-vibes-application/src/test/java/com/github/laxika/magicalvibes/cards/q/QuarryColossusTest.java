package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuarryColossusTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts target creature beneath the number of cards equal to Plains controlled")
    void putsTargetCreatureBeneathPlainsCount() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setLibrary(player2, new Island(), new Island(), new Island());

        castQuarryColossus(bears);

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB puts target creature on top when no Plains are controlled")
    void putsTargetCreatureOnTopWithNoPlains() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setLibrary(player2, new Island(), new Island());

        castQuarryColossus(bears);

        assertThat(gd.playerDecks.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castQuarryColossus(Permanent target) {
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(List.of(cards));
    }
}
