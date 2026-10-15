package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenerableKnight.class, YouthfulKnight.class, SporecapSpider.class})
class VenerableKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger puts a +1/+1 counter on a Knight you control")
    void deathTriggerPutsCounterOnKnightYouControl() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetKnight.getId());
        harness.passBothPriorities();

        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger cannot target a non-Knight creature")
    void deathTriggerCannotTargetNonKnight() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, spider.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death trigger cannot target an opponent's Knight")
    void deathTriggerCannotTargetOpponentsKnight() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponentsKnight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsKnight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death trigger has no legal target when only opposing Knights remain")
    void deathTriggerWithNoLegalTargetDoesNotPrompt() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent opponentsKnight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponentsKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Venerable Knight");
    }

    @Test
    @DisplayName("Death trigger does not put a counter on a target that has left the battlefield")
    void targetLeavingBattlefieldPreventsCounterPlacement() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetKnight.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, targetKnight));
        harness.passBothPriorities();

        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Youthful Knight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    @DisplayName("Death trigger can target a noncreature kindred Knight permanent")
    void deathTriggerCanTargetNoncreatureKnight() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, targetKnight.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "KNIGHT");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetKnight.getId());
        harness.passBothPriorities();

        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
