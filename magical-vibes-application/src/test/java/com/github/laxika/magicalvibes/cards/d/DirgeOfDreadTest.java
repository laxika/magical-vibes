package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarrenMoor;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DirgeOfDread.class, GlorySeeker.class, BarrenMoor.class, Shock.class})
class DirgeOfDreadTest extends BaseCardTest {

    @Test
    void allCreaturesGainFearUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FEAR)).isFalse();
    }

    @Test
    void cyclingMayGiveTargetCreatureFearBeforeDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        cycleWithFearTarget(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
        harness.assertInGraveyard(player1, "Dirge of Dread");
        harness.assertNotInHand(player1, "Glory Seeker");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    void cyclingFearWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        cycleWithFearTarget(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isFalse();
    }

    @Test
    void cyclingFearCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BarrenMoor());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dirge of Dread");
        harness.assertNotInHand(player1, "Glory Seeker");
    }

    @Test
    void cyclingWithoutTargetStillDrawsACard() {
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dirge of Dread");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    void cyclingStillDrawsWhenFearTargetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        cycleWithFearTarget(target);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Dirge of Dread");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    void cyclingFearCanBeDeclinedAfterChoosingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isFalse();
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    void creaturesEnteringAfterSpellResolvesDoNotGainFear() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        assertThat(gqs.hasKeyword(gd, original, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.FEAR)).isFalse();
    }

    private void cycleWithFearTarget(Permanent target) {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
    }
}
