package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WingrattleScarecrowTest extends BaseCardTest {

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent scarecrow() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wingrattle Scarecrow"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("No flying and no persist without blue or black creatures")
    void noKeywordsWithoutColoredCreatures() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Has flying while controller controls a blue creature")
    void flyingWithBlueCreature() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Has persist while controller controls a black creature")
    void persistWithBlackCreature() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's blue creature does not grant flying")
    void opponentBlueCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player2, new FugitiveWizard());

        assertThat(gqs.hasKeyword(gd, scarecrow(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Persist returns the Scarecrow with a -1/-1 counter when it dies controlling a black creature")
    void persistReturnsWhenControllingBlackCreature() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Wingrattle Scarecrow"));
        resolveUntilInputOrEmpty();

        Permanent scarecrow = scarecrow();
        assertThat(scarecrow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
