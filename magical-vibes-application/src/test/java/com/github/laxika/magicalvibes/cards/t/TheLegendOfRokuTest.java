package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarRoku;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLegendOfRoku.class, AvatarRoku.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Mountain.class})
class TheLegendOfRokuTest extends BaseCardTest {

    @Test
    void chapterOneExilesTopThreeCardsForPlay() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card third = new HillGiant();
        Card remaining = new Mountain();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
    }

    @Test
    void chapterTwoAddsOneManaOfChosenColor() {
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void chapterThreeTransformsIntoAvatarRoku() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = findPermanent(player1, "Avatar Roku");
        assertThat(avatar.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Legend of Roku");
    }

    @Test
    void chapterThreeReturnsOpponentOwnedSagaUnderAbilityControllersControl() {
        TheLegendOfRoku card = new TheLegendOfRoku();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar Roku");
        harness.assertNotOnBattlefield(player2, "Avatar Roku");
        harness.assertNotOnBattlefield(player1, "The Legend of Roku");
    }

    @Test
    void castingSagaTriggersChapterOneOnEntering() {
        Card exiled = new Mountain();
        harness.setLibrary(player1, List.of(exiled));

        harness.castFromHand(player1, new TheLegendOfRoku(), "{2}{R}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(findPermanent(player1, "The Legend of Roku").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void chapterOneAllowsPlayingExiledLandAndCastingSpellWithNormalManaCost() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, creature));
        addSaga(0);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.castFromExile(player1, land.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void chapterOneDoesNotAllowCastingSpellWithoutPayingMana() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        addSaga(0);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
    }

    @Test
    void chapterOnePermissionSurvivesSourceLeavingAndExpiresAfterControllersNextTurn() {
        Card exiled = new Mountain();
        harness.setLibrary(player1, List.of(exiled));
        addSaga(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }

    @Test
    void chapterThreeReturnsANewSummoningSickPermanentWithoutLoreCounters() {
        Permanent saga = addSaga(2);
        saga.tap();

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = findPermanent(player1, "Avatar Roku");
        assertThat(avatar.getId()).isNotEqualTo(saga.getId());
        assertThat(avatar.getCounterCount(CounterType.LORE)).isZero();
        assertThat(avatar.isTapped()).isFalse();
        assertThat(avatar.isSummoningSick()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void dragonTokenCannotBeBlockedByGroundCreature() {
        addTransformedAvatar();
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        findPermanent(player1, "Dragon").setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void avatarRokuFirebendingAddsFourRedManaUntilCombatEnds() {
        Permanent avatar = addTransformedAvatar();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(avatar.isTapped()).isTrue();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void avatarRokuCreatesFirebendingDragonToken() {
        Permanent avatar = addTransformedAvatar();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        dragon.setSummoningSick(false);

        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(avatar.isTapped()).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLegendOfRoku());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedAvatar() {
        TheLegendOfRoku front = new TheLegendOfRoku();
        Permanent avatar = new Permanent(front);
        avatar.setCard(front.getBackFaceCard());
        avatar.setTransformed(true);
        avatar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(avatar);
        return avatar;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
