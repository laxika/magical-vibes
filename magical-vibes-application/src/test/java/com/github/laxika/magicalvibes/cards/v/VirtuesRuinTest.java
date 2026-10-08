package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DevotedHero;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirtuesRuin.class, DevotedHero.class, HillGiant.class, Plains.class})
class VirtuesRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys white creatures controlled by both players")
    void destroysWhiteCreatures() {
        harness.addToBattlefield(player1, new DevotedHero());
        harness.addToBattlefield(player2, new DevotedHero());
        harness.castFromHand(player1, new VirtuesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Hero");
        harness.assertNotOnBattlefield(player2, "Devoted Hero");
        harness.assertInGraveyard(player1, "Devoted Hero");
        harness.assertInGraveyard(player2, "Devoted Hero");
    }

    @Test
    @DisplayName("Leaves non-white creatures untouched")
    void leavesNonWhiteCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.castFromHand(player1, new VirtuesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Leaves noncreature permanents untouched")
    void leavesNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Plains());
        harness.castFromHand(player1, new VirtuesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Destroys only white creatures among a mixed board")
    void destroysOnlyWhiteAmongMixed() {
        harness.addToBattlefield(player1, new DevotedHero());
        harness.addToBattlefield(player1, new HillGiant());
        harness.castFromHand(player1, new VirtuesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Hero");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Resolves without any creatures on the battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.castFromHand(player1, new VirtuesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Virtue's Ruin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroys every white creature when one player controls several")
    void destroysMultipleWhiteCreaturesUnderOneController() {
        var first = harness.addToBattlefieldAndReturn(player2, new DevotedHero());
        var second = harness.addToBattlefieldAndReturn(player2, new DevotedHero());
        harness.castFromHand(player1, new VirtuesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devoted Hero");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(first.getCard(), second.getCard());
    }
}
