package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.Turnabout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BackToBasics.class, BlastedLandscape.class, Cathodion.class, Forest.class, Turnabout.class})
class BackToBasicsTest extends BaseCardTest {

    @Test
    @DisplayName("Spells can untap nonbasic lands while Back to Basics is on the battlefield")
    void allowsUntappingBySpell() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new BlastedLandscape());
        nonbasicLand.tap();
        harness.setHand(player1, List.of(new Turnabout()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, "UNTAP_LAND");

        assertThat(nonbasicLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Back to Basics does not tap an untapped nonbasic land")
    void doesNotTapUntappedNonbasicLands() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new BlastedLandscape());

        advanceToUpkeep(player1);

        assertThat(nonbasicLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nonbasic lands stay tapped while basic lands untap")
    void nonbasicLandsStayTapped() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new BlastedLandscape());
        basicLand.tap();
        nonbasicLand.tap();

        advanceToUpkeep(player1);

        assertThat(basicLand.isTapped()).isFalse();
        assertThat(nonbasicLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The effect applies during an opponent's untap step")
    void affectsOpponentsNonbasicLands() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new BlastedLandscape());
        opponentLand.tap();

        advanceToUpkeep(player2);

        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonland permanents untap normally")
    void doesNotAffectNonlandPermanents() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Cathodion());
        artifactCreature.tap();

        advanceToUpkeep(player1);

        assertThat(artifactCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nonbasic lands untap again after Back to Basics leaves the battlefield")
    void stopsApplyingAfterLeavingBattlefield() {
        Permanent backToBasics = harness.addToBattlefieldAndReturn(player1, new BackToBasics());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new BlastedLandscape());
        nonbasicLand.tap();
        gd.playerBattlefields.get(player1.getId()).remove(backToBasics);

        advanceToUpkeep(player1);

        assertThat(nonbasicLand.isTapped()).isFalse();
    }
}
