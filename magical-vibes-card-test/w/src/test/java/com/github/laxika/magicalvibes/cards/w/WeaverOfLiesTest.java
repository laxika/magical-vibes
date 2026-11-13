package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaverOfLies.class, WallOfDeceit.class, AvenEnvoy.class})
class WeaverOfLiesTest extends BaseCardTest {

    @Test
    void turnsAnyNumberOfOtherMorphCreaturesFaceDown() {
        Permanent ownMorphCreature = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        Permanent opposingMorphCreature = harness.addToBattlefieldAndReturn(player2, new WallOfDeceit());
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(ownMorphCreature.getId(), opposingMorphCreature.getId())
                .doesNotContain(weaver.getId(), ordinaryCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownMorphCreature.getId(),
                opposingMorphCreature.getId()));
        harness.passBothPriorities();

        assertThat(ownMorphCreature.isFaceDown()).isTrue();
        assertThat(opposingMorphCreature.isFaceDown()).isTrue();
        assertThat(ordinaryCreature.isFaceDown()).isFalse();
        assertThat(weaver.isFaceDown()).isFalse();
        assertThat(ownMorphCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownMorphCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void canChooseNoMorphCreatures() {
        Permanent morphCreature = harness.addToBattlefieldAndReturn(player2, new WallOfDeceit());

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(morphCreature.getId())
                .doesNotContain(weaver.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(morphCreature.isFaceDown()).isFalse();
        assertThat(weaver.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpWithNoOtherMorphCreaturesDoesNotAskForTargets() {
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(ordinaryCreature.isFaceDown()).isFalse();
        assertThat(weaver.isFaceDown()).isFalse();
    }

    @Test
    void canChooseOnlyOneOfSeveralEligibleCreatures() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new WallOfDeceit());

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(chosen.isFaceDown()).isTrue();
        assertThat(unchosen.isFaceDown()).isFalse();
        assertThat(weaver.isFaceDown()).isFalse();
    }

    @Test
    void faceDownCreaturesDoNotHaveMorphAbilitiesForTargetSelection() {
        Permanent faceDownWall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        Permanent faceUpWall = harness.addToBattlefieldAndReturn(player2, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(faceDownWall.isFaceDown()).isTrue();

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(faceUpWall.getId())
                .doesNotContain(faceDownWall.getId(), weaver.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(faceUpWall.getId()));
        harness.passBothPriorities();

        assertThat(faceDownWall.isFaceDown()).isTrue();
        assertThat(faceUpWall.isFaceDown()).isTrue();
    }

    @Test
    void affectedCreatureCanTurnFaceUpAgainWithoutChangingItsIdentityOrTappedStatus() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        wall.tap();

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));
        harness.handleMultiplePermanentsChosen(player1, List.of(wall.getId()));
        harness.passBothPriorities();

        assertThat(wall.isFaceDown()).isTrue();
        assertThat(wall.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wall));

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(wall.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Wall of Deceit")).isSameAs(wall);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void anyNumberOfTargetsCanExceedNinetyNine() {
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new WallOfDeceit()));
        }

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        // "Any number of target creatures": all of them are announced together (CR 601.2c via CR 603.3d).
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isGreaterThanOrEqualTo(100);
        harness.handleMultiplePermanentsChosen(player1, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(targets).allSatisfy(target -> assertThat(target.isFaceDown()).isTrue());
        assertThat(weaver.isFaceDown()).isFalse();
    }
}
