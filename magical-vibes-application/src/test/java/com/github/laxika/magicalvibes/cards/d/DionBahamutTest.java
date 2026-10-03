package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BahamutWardenOfLight;
import com.github.laxika.magicalvibes.cards.c.ContainmentPriest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DionBahamut.class, BahamutWardenOfLight.class, ContainmentPriest.class,
        FountainOfYouth.class, GrizzlyBears.class})
class DionBahamutTest extends BaseCardTest {

    @Test
    void entersWithKnightAndGivesDionAndKnightsFlyingDuringYourTurn() {
        Permanent ownNonKnight = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDion();

        Permanent dion = findPermanent(player1, DionBahamut.class);
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dion, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonKnight, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, dion, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    void transformsIntoBahamutAndResolvesFirstChapter() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDion();
        Permanent dion = findPermanent(player1, DionBahamut.class);
        dion.setSummoningSick(false);

        addTransformMana();
        harness.activateAbility(player1, indexOf(player1, dion), 0, null, null);
        harness.passBothPriorities();

        Permanent bahamut = findPermanent(player1, BahamutWardenOfLight.class);
        assertThat(bahamut.isTransformed()).isTrue();
        assertThat(bahamut.getCounterCount(CounterType.LORE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
        assertThat(bahamut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void containmentPriestExilesDionInsteadOfReturningBahamut() {
        harness.addToBattlefield(player1, new ContainmentPriest());
        castDion();
        Permanent dion = findPermanent(player1, DionBahamut.class);
        Card physicalCard = dion.getOriginalCard();
        dion.setSummoningSick(false);

        addTransformMana();
        harness.activateAbility(player1, indexOf(player1, dion), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(physicalCard.getId()));
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card())
                .containsExactly(physicalCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void thirdChapterDestroysTargetAndReturnsBahamutToFrontFace() {
        addBahamutWithLore(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent dion = findPermanent(player1, DionBahamut.class);
        assertThat(dion.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    void thirdChapterDoesNotReturnBahamutIfTargetIsIllegal() {
        addBahamutWithLore(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof DionBahamut);
    }

    @Test
    void secondChapterAffectsCreaturesPresentAtResolutionAndFlyingExpires() {
        Permanent bahamut = addBahamutWithLore(1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        advanceToNextChapter();
        Permanent creatureBeforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(bahamut.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(bahamut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creatureBeforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creatureBeforeResolution, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(laterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creatureBeforeResolution, Keyword.FLYING)).isFalse();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void newlyCastDionCannotPayTapCost() {
        castDion();
        Permanent dion = findPermanent(player1, DionBahamut.class);
        addTransformMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, dion), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dion.isTapped()).isFalse();
    }

    @Test
    void transformCannotBeActivatedOutsideMainPhase() {
        castDion();
        Permanent dion = findPermanent(player1, DionBahamut.class);
        dion.setSummoningSick(false);
        addTransformMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, dion), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dion.isTapped()).isFalse();
    }

    @Test
    void returnedBahamutCannotAttackOnTheTurnItEnters() {
        castDion();
        Permanent dion = findPermanent(player1, DionBahamut.class);
        dion.setSummoningSick(false);
        addTransformMana();
        harness.activateAbility(player1, indexOf(player1, dion), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bahamut = findPermanent(player1, BahamutWardenOfLight.class);
        assertThatThrownBy(() -> declareAttackers(List.of(indexOf(player1, bahamut))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedDionCannotImmediatelyTransformAgain() {
        addBahamutWithLore(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dion = findPermanent(player1, DionBahamut.class);
        addTransformMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, dion), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thirdChapterCanDestroyBahamutItselfWithoutReturningIt() {
        Permanent bahamut = addBahamutWithLore(2);
        Card physicalCard = bahamut.getOriginalCard();
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, bahamut.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(physicalCard);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void transformRequiresAnEmptyStack() {
        Permanent dion = addCreatureReady(player1, new DionBahamut());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        addTransformMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, dion), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dion.isTapped()).isFalse();
    }

    private void castDion() {
        harness.castFromHand(player1, new DionBahamut(), "{3}{W}");
        resolveAllTriggers();
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private Permanent addBahamutWithLore(int loreCounters) {
        DionBahamut card = new DionBahamut();
        Permanent bahamut = new Permanent(card);
        bahamut.setCard(card.getBackFaceCard());
        bahamut.setTransformed(true);
        bahamut.setSummoningSick(false);
        bahamut.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(bahamut);
        return bahamut;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, Class<?> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
