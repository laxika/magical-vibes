package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarKuruk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLegendOfKuruk.class, AvatarKuruk.class, GrizzlyBears.class, KamiOfFalseHope.class})
class TheLegendOfKurukTest extends BaseCardTest {

    @Test
    void chapterOneScryThenDraws() {
        Card top = new GrizzlyBears();
        Card second = new KamiOfFalseHope();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    void chapterTwoScryThenDraws() {
        Card top = new GrizzlyBears();
        Card second = new KamiOfFalseHope();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    void chapterThreeTransformsTheSaga() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = findPermanent(player1, "Avatar Kuruk");
        assertThat(avatar.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Legend of Kuruk");
    }

    @Test
    void avatarCreatesRestrictedSpiritTokenWhenYouCastASpell() {
        addTransformedAvatar(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getSubtypes()).containsExactlyElementsOf(List.of(
                com.github.laxika.magicalvibes.model.CardSubtype.SPIRIT));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void restrictedSpiritCannotBeBlockedByNonSpirit() {
        addTransformedAvatar(player1);
        createSpiritToken(player1);
        Permanent spirit = findPermanent(player1, "Spirit");
        spirit.setSummoningSick(false);
        spirit.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(spirit)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedSpiritCanBeBlockedBySpirit() {
        addTransformedAvatar(player1);
        createSpiritToken(player1);
        Permanent spirit = findPermanent(player1, "Spirit");
        spirit.setSummoningSick(false);
        spirit.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KamiOfFalseHope());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(spirit))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void restrictedSpiritCanBlockOnlySpirit() {
        addTransformedAvatar(player2);
        createSpiritToken(player2);
        Permanent spirit = findPermanent(player2, "Spirit");
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(spirit),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void waterbendTakesAnExtraTurnAndExhaustsOnlyOnce() {
        Permanent avatar = addTransformedAvatar(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 18);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null);

        assertThat(List.of(avatar, first, second)).filteredOn(Permanent::isTapped).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void chapterOneCanBottomBothCardsBeforeDrawing() {
        Card first = new TheLegendOfKuruk();
        Card second = new TheLegendOfKuruk();
        Card third = new TheLegendOfKuruk();
        harness.setLibrary(player1, List.of(first, second, third));
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    void enteringSagaTriggersChapterOneAndDrawsAfterScry() {
        Card first = new TheLegendOfKuruk();
        Card second = new TheLegendOfKuruk();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TheLegendOfKuruk()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "The Legend of Kuruk").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
    }

    @Test
    void chapterThreeReturnsUnderAbilityControllersControlInsteadOfOwners() {
        TheLegendOfKuruk card = new TheLegendOfKuruk();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar Kuruk");
        harness.assertNotOnBattlefield(player2, "Avatar Kuruk");
        Permanent avatar = findPermanent(player1, "Avatar Kuruk");
        assertThat(avatar.getId()).isNotEqualTo(saga.getId());
        assertThat(avatar.getCounterCount(CounterType.LORE)).isZero();
        assertThat(avatar.isSummoningSick()).isTrue();
    }

    @Test
    void avatarTriggersForNoncreatureSpellBeforeSpellResolves() {
        addTransformedAvatar(player1);
        harness.setHand(player1, List.of(new TheLegendOfKuruk()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "The Legend of Kuruk");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsSpellDoesNotCreateSpirit() {
        addTransformedAvatar(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void restrictedSpiritCanBlockSpirit() {
        addTransformedAvatar(player2);
        createSpiritToken(player2);
        Permanent spirit = findPermanent(player2, "Spirit");
        Permanent attacker = addCreatureReady(player1, new KamiOfFalseHope());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(spirit),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    void waterbendCanBePaidEntirelyWithManaWithoutTappingAvatar() {
        Permanent avatar = addTransformedAvatar(player1);
        avatar.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null);
        harness.passBothPriorities();

        assertThat(avatar.isTapped()).isFalse();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void waterbendCanTapSummoningSickAvatarAndExhaustRemainsUsedAfterUntapping() {
        Permanent avatar = addTransformedAvatar(player1);
        avatar.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 19);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null);
        harness.passBothPriorities();

        assertThat(avatar.isTapped()).isTrue();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.performUntapStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLegendOfKuruk());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedAvatar(com.github.laxika.magicalvibes.model.Player player) {
        TheLegendOfKuruk front = new TheLegendOfKuruk();
        Permanent avatar = new Permanent(front);
        avatar.setCard(front.getBackFaceCard());
        avatar.setTransformed(true);
        avatar.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(avatar);
        return avatar;
    }

    private void createSpiritToken(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
