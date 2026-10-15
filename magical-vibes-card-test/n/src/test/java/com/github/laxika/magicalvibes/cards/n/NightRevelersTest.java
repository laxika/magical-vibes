package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightRevelers.class, DoomedTraveler.class, AbbeyGriffin.class})
class NightRevelersTest extends BaseCardTest {

    @Test
    @DisplayName("Night Revelers gains haste when an opponent's Human enters later")
    void gainsHasteWhenOpponentHumanEntersLater() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();

        harness.enterBattlefieldAndReturn(player2, new DoomedTraveler());

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Night Revelers has haste when an opponent controls a Human")
    void hasHasteWhenOpponentControlsHuman() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());
        harness.addToBattlefield(player2, new DoomedTraveler()); // Human Soldier

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Night Revelers does NOT have haste when no opponent controls a Human")
    void noHasteWithoutOpponentHuman() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());
        harness.addToBattlefield(player2, new AbbeyGriffin()); // Griffin, not Human

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Night Revelers does NOT have haste when only controller controls a Human")
    void noHasteWhenControllerControlsHuman() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());
        harness.addToBattlefield(player1, new DoomedTraveler()); // Own Human does not count

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Night Revelers alone does NOT have haste")
    void noHasteAlone() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Night Revelers loses haste when opponent's Human leaves the battlefield")
    void losesHasteWhenOpponentHumanLeaves() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());
        harness.addToBattlefield(player2, new DoomedTraveler());

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        // Remove the opponent's Human
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Doomed Traveler"));

        // Haste should be gone immediately (computed on the fly)
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Losing one opponent Human while another exists still grants haste")
    void stillHasHasteWithMultipleOpponentHumans() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());
        Permanent firstHuman = harness.addToBattlefieldAndReturn(player2, new DoomedTraveler());
        harness.addToBattlefield(player2, new DoomedTraveler());

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        // Remove one Human
        gd.playerBattlefields.get(player2.getId()).remove(firstHuman);

        // Another opponent Human still grants haste
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Static haste survives end-of-turn modifier reset")
    void staticHasteSurvivesEndOfTurnReset() {
        Permanent revelers = harness.addToBattlefieldAndReturn(player1, new NightRevelers());
        harness.addToBattlefield(player2, new DoomedTraveler());

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        // Simulate end-of-turn cleanup
        revelers.resetModifiers();

        // Static haste should still be computed
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

}
