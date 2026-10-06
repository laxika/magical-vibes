package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentsGift.class, GrizzlyBears.class, FountainOfYouth.class, KrakenHatchling.class})
class SerpentsGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants deathtouch to the target creature")
    void resolvingGrantsDeathtouch() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SerpentsGift()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SerpentsGift()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SerpentsGift()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SerpentsGift()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchDestroysBlockerWithGreaterToughness() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new KrakenHatchling());
        harness.setHand(player1, List.of(new SerpentsGift()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Kraken Hatchling");
        harness.assertNotOnBattlefield(player2, "Kraken Hatchling");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotGrantDeathtouchToAnotherCreatureWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SerpentsGift()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        harness.assertInGraveyard(player1, "Serpent's Gift");
    }
}
