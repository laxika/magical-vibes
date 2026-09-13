package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishLyrist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Congregate.class, ElvishLyrist.class, Forest.class})
class CongregateTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 2 life for each creature on the battlefield, both sides counted")
    void gainsTwoPerCreatureOnBattlefield() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ElvishLyrist());
        harness.addToBattlefield(player1, new ElvishLyrist());
        harness.addToBattlefield(player2, new ElvishLyrist());

        harness.setHand(player1, List.of(new Congregate()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Does not count noncreature permanents on the battlefield")
    void countsOnlyCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ElvishLyrist());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new Congregate()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Can target an opponent, who gains the life")
    void canTargetOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new ElvishLyrist());

        harness.setHand(player1, List.of(new Congregate()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Gains no life when no creatures are on the battlefield")
    void noCreaturesGainsNothing() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Congregate()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Congregate cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishLyrist());

        harness.setHand(player1, List.of(new Congregate()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
