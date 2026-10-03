package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlacklanceParagon.class, YouthfulKnight.class, GrizzlyBears.class})
class BlacklanceParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability grants a Knight deathtouch and lifelink until end of turn")
    void grantsKeywordsToTargetKnightUntilEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new BlacklanceParagon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, knight.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isTrue();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Knight permanent")
    void cannotTargetNonKnight() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlacklanceParagon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Knight");
    }

    @Test
    @DisplayName("Can flash in during the end step and target itself")
    void flashesInAndTargetsItself() {
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new BlacklanceParagon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent paragon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Blacklance Paragon"));
        harness.handlePermanentChosen(player1, paragon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.LIFELINK)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still grants both keywords if Blacklance Paragon leaves before resolution")
    void triggerResolvesWithoutItsSource() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.setHand(player1, List.of(new BlacklanceParagon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, knight.getId());
        harness.passBothPriorities();
        Permanent paragon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Blacklance Paragon"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, paragon));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A removed target does not cause the trigger to grant keywords to a different Knight")
    void removedTargetDoesNotRedirectGrant() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new BlacklanceParagon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, knight.getId());
        harness.passBothPriorities();
        Permanent paragon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Blacklance Paragon"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, knight));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
