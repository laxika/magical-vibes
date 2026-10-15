package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortentOfBetrayal.class, NessianCourser.class, Mountain.class})
class PortentOfBetrayalTest extends BaseCardTest {

    @BeforeEach
    void prepareLibrary() {
        harness.setLibrary(player1, List.of(new Mountain(), new NessianCourser()));
    }

    @Test
    @DisplayName("Steals, untaps and grants haste before scrying")
    void stealsUntapsAndHastesBeforeScrying() {
        Permanent target = addCreature(new NessianCourser(), player2);
        target.tap();

        castPortent(target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        finishScry();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreature(new NessianCourser(), player2);

        castPortent(target.getId());
        finishScry();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Card noncreature = new Mountain();
        Permanent land = harness.addToBattlefieldAndReturn(player2, noncreature);

        assertThatThrownBy(() -> castPortent(land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canUntapAndHasteYourOwnCreature() {
        Permanent target = addCreature(new NessianCourser(), player1);
        target.setSummoningSick(true);
        target.tap();

        castPortent(target.getId());
        finishScry();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void canKeepTopCardWhenScrying() {
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        Permanent target = addCreature(new NessianCourser(), player2);

        castPortent(target.getId());
        finishScry();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalLibrary);
        harness.assertInGraveyard(player1, "Portent of Betrayal");
    }

    @Test
    void canPutTopCardOnBottomWhenScrying() {
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        Permanent target = addCreature(new NessianCourser(), player2);

        castPortent(target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(originalLibrary.get(1), originalLibrary.get(0));
    }

    @Test
    void emptyLibraryDoesNotPreventControlUntapOrHaste() {
        harness.setLibrary(player1, List.of());
        Permanent target = addCreature(new NessianCourser(), player2);
        target.tap();

        castPortent(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Portent of Betrayal");
    }

    @Test
    void doesNotScryWhenTargetLeavesBeforeResolution() {
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        Permanent target = addCreature(new NessianCourser(), player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PortentOfBetrayal()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalLibrary);
        harness.assertInGraveyard(player1, "Portent of Betrayal");
    }

    private Permanent addCreature(Card card, Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castPortent(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PortentOfBetrayal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void finishScry() {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
