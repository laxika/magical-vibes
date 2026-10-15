package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PterodonKnight.class, RaptorCompanion.class})
class PterodonKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying when controller controls a Dinosaur")
    void hasFlyingWithDinosaur() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        harness.addToBattlefield(player1, new RaptorCompanion());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("No flying without a Dinosaur")
    void noFlyingWithoutDinosaur() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with a non-Dinosaur creature")
    void noFlyingWithNonDinosaurCreature() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        harness.addToBattlefield(player1, new PterodonKnight());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying when Dinosaur leaves the battlefield")
    void losesFlyingWhenDinosaurLeaves() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Dinosaur does not grant flying")
    void opponentDinosaurDoesNotCount() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        harness.addToBattlefield(player2, new RaptorCompanion());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying immediately when a Dinosaur enters later")
    void gainsFlyingWhenDinosaurEnters() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Retains flying while at least one Dinosaur remains")
    void retainsFlyingWithRemainingDinosaur() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Dinosaurs in hand and graveyard do not grant flying")
    void dinosaursOutsideBattlefieldDoNotCount() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.setGraveyard(player1, List.of(new RaptorCompanion()));

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is granted only to the Knight, not its Dinosaur")
    void flyingAppliesOnlyToKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new PterodonKnight());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.FLYING)).isFalse();
    }

}
