package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KirinTouchedOrochi;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({TeachingsOfTheKirin.class, KirinTouchedOrochi.class, GrizzlyBears.class, LightningBolt.class})
class TeachingsOfTheKirinTest extends BaseCardTest {

    private static final String CREATURE_MODE =
            "Exile target creature card from a graveyard. When you do, create a 1/1 colorless Spirit creature token.";
    private static final String NONCREATURE_MODE =
            "Exile target noncreature card from a graveyard. When you do, put a +1/+1 counter on target creature you control.";

    @Test
    @DisplayName("Chapter I mills three cards and creates a Spirit")
    void chapterIMillsAndCreatesSpirit() {
        com.github.laxika.magicalvibes.model.Card firstMilled = new GrizzlyBears();
        com.github.laxika.magicalvibes.model.Card secondMilled = new LightningBolt();
        com.github.laxika.magicalvibes.model.Card thirdMilled = new LightningBolt();
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
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

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
        com.github.laxika.magicalvibes.model.Card creatureCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creatureCard));
        Permanent kirin = addReadyKirin(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kirin)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CREATURE_MODE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("The noncreature attack mode exiles the card before targeting a creature")
    void attackNoncreatureModeCountersTargetCreatureYouControl() {
        com.github.laxika.magicalvibes.model.Card noncreatureCard = new LightningBolt();
        harness.setGraveyard(player2, List.of(noncreatureCard));
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
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

        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TeachingsOfTheKirin());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addReadyKirin(Player player) {
        TeachingsOfTheKirin front = new TeachingsOfTheKirin();
        Permanent kirin = new Permanent(front);
        kirin.setCard(front.getBackFaceCard());
        kirin.setTransformed(true);
        kirin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(kirin);
        return kirin;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
