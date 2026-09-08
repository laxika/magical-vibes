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
        Permanent bahamut = addBahamutWithLore(2);
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

    private void castDion() {
        harness.setHand(player1, List.of(new DionBahamut()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
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
