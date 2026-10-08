package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CommuneWithSpirits;
import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.cards.k.KirinTouchedOrochi;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeachingsOfTheKirin.class, KirinTouchedOrochi.class, FangOfShigeki.class, CommuneWithSpirits.class})
class TeachingsOfTheKirinTest extends BaseCardTest {

    private static final String CREATURE_MODE =
            "Exile target creature card from a graveyard. When you do, create a 1/1 colorless Spirit creature token.";
    private static final String NONCREATURE_MODE =
            "Exile target noncreature card from a graveyard. When you do, put a +1/+1 counter on target creature you control.";

    @Test
    @DisplayName("Chapter I mills three cards and creates a Spirit")
    void chapterIMillsAndCreatesSpirit() {
        com.github.laxika.magicalvibes.model.Card firstMilled = new FangOfShigeki();
        com.github.laxika.magicalvibes.model.Card secondMilled = new CommuneWithSpirits();
        com.github.laxika.magicalvibes.model.Card thirdMilled = new CommuneWithSpirits();
        harness.setLibrary(player1, List.of(firstMilled, secondMilled, thirdMilled));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilled, secondMilled, thirdMilled);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Chapter II targets exactly one creature you control")
    void chapterIITargetsCreatureYouControl() {
        addSagaWithLore(1);
        Permanent ownCreature = addCreatureReady(player1, new FangOfShigeki());
        Permanent opposingCreature = addCreatureReady(player2, new FangOfShigeki());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III transforms the Saga")
    void chapterIIITransforms() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent backFace = findPermanent(player1, "Kirin-Touched Orochi");
        assertThat(backFace).isNotNull();
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The creature-card attack mode exiles the card and creates a Spirit")
    void attackCreatureModeCreatesSpirit() {
        com.github.laxika.magicalvibes.model.Card creatureCard = new FangOfShigeki();
        harness.setGraveyard(player2, List.of(creatureCard));
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CREATURE_MODE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Fang of Shigeki");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("The noncreature attack mode exiles the card before targeting a creature")
    void attackNoncreatureModeCountersTargetCreatureYouControl() {
        com.github.laxika.magicalvibes.model.Card noncreatureCard = new CommuneWithSpirits();
        harness.setGraveyard(player2, List.of(noncreatureCard));
        Permanent target = addCreatureReady(player1, new FangOfShigeki());
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, NONCREATURE_MODE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(noncreatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(noncreatureCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId(), kirin.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Commune with Spirits");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter I creates a Spirit even with an empty library")
    void chapterICreatesSpiritWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addSagaWithLore(0);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter II without a legal target does not prevent chapter III")
    void chapterIIWithoutCreatureStillAdvancesToChapterIII() {
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Kirin-Touched Orochi").isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Chapter III returns a new untapped creature without the Saga's counters")
    void chapterIIIReturnsNewPermanent() {
        Permanent saga = addSagaWithLore(2);
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        saga.tap();

        advanceToNextChapter();
        resolveAllTriggers();

        Permanent kirin = findPermanent(player1, "Kirin-Touched Orochi");
        assertThat(kirin.getId()).isNotEqualTo(saga.getId());
        assertThat(kirin.isTapped()).isFalse();
        assertThat(kirin.isSummoningSick()).isTrue();
        assertThat(kirin.getCounterCount(CounterType.LORE)).isZero();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Teachings of the Kirin");
    }

    @Test
    @DisplayName("Exiling a creature card queues a separate Spirit-creation trigger")
    void creatureModeAllowsResponseBeforeSpiritCreation() {
        var creatureCard = new FangOfShigeki();
        var noncreatureCard = new CommuneWithSpirits();
        harness.setGraveyard(player1, List.of(creatureCard, noncreatureCard));
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.handleListChoice(player1, CREATURE_MODE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Fang of Shigeki");
        assertThat(gd.findExiledCard(creatureCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("A creature card removed in response produces no Spirit")
    void creatureModeDoesNothingWhenTargetLeavesGraveyard() {
        var creatureCard = new FangOfShigeki();
        harness.setGraveyard(player2, List.of(creatureCard));
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.handleListChoice(player1, CREATURE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.setGraveyard(player2, List.of());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The noncreature mode can exile from your graveyard and target Orochi")
    void noncreatureModeCanCounterOrochi() {
        var noncreatureCard = new CommuneWithSpirits();
        var creatureCard = new FangOfShigeki();
        harness.setGraveyard(player1, List.of(noncreatureCard, creatureCard));
        Permanent opposingCreature = addCreatureReady(player2, new FangOfShigeki());
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.handleListChoice(player1, NONCREATURE_MODE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(noncreatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(noncreatureCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(noncreatureCard.getId())).isNotNull();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(kirin.getId());
        harness.handlePermanentChosen(player1, kirin.getId());
        resolveAllTriggers();

        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Fang of Shigeki");
    }

    @Test
    @DisplayName("A noncreature card removed in response produces no counter trigger")
    void noncreatureModeDoesNothingWhenTargetLeavesGraveyard() {
        var noncreatureCard = new CommuneWithSpirits();
        harness.setGraveyard(player2, List.of(noncreatureCard));
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.handleListChoice(player1, NONCREATURE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(noncreatureCard.getId()));
        harness.setGraveyard(player2, List.of());

        resolveAllTriggers();

        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TeachingsOfTheKirin());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addReadyKirin(Player player) {
        TeachingsOfTheKirin front = new TeachingsOfTheKirin();
        Permanent kirin = harness.addToBattlefieldAndReturn(player, front);
        kirin.setCard(front.getBackFaceCard());
        kirin.setTransformed(true);
        kirin.setSummoningSick(false);
        return kirin;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
