package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LazotepConvert;
import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfAmonkhet.class, LazotepConvert.class, GrizzlyBears.class, Shock.class,
        PortentTracker.class, VolcanicSpite.class})
class InvasionOfAmonkhetTest extends BaseCardTest {

    @Test
    void entersAndEachPlayerMillsThreeOpponentsDiscardAndControllerDraws() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), drawn));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new InvasionOfAmonkhet(), "{1}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void transformedCreatureMayCopyACreatureCardFromAnyGraveyardWithThePrintedExceptions() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent converted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(converted.getCard()).isNotInstanceOf(LazotepConvert.class);
        assertThat(converted.getCard().getPower()).isEqualTo(4);
        assertThat(converted.getCard().getToughness()).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, converted))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(converted.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears);
    }

    @Test
    void decliningTheCopyLeavesTheTransformedBackFaceUncopied() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);

        harness.handleMayAbilityChosen(player1, false);

        Permanent converted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(converted.getCard()).isInstanceOf(LazotepConvert.class);
    }

    @Test
    void emptyOpponentHandDoesNotPreventDrawingAfterBothPlayersMill() {
        Card drawn = new PortentTracker();
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new VolcanicSpite(), new VolcanicSpite(),
                new VolcanicSpite(), drawn));
        harness.setLibrary(player2, List.of(new PortentTracker()));

        harness.castFromHand(player1, new InvasionOfAmonkhet(), "{1}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void copyChoiceIncludesOwnCreaturesAndExcludesNoncreatureCards() {
        Card tracker = new PortentTracker();
        Card instant = new VolcanicSpite();
        harness.setGraveyard(player1, List.of(tracker, instant));
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(tracker.getId());
        harness.handleMultipleCardsChosen(player1, List.of(tracker.getId()));

        Permanent converted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed).findFirst().orElseThrow();
        assertThat(converted.getCard().getPower()).isEqualTo(4);
        assertThat(converted.getCard().getToughness()).isEqualTo(4);
        assertThat(converted.getCard().getSubtypes())
                .contains(CardSubtype.SATYR, CardSubtype.SCOUT, CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectiveColors(gd, converted))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tracker, instant);
    }

    @Test
    void noCreatureCardsInGraveyardsLeavesTheBackFaceUncopied() {
        harness.setGraveyard(player1, List.of(new VolcanicSpite()));
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);

        Permanent converted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed).findFirst().orElseThrow();
        assertThat(converted.getCard()).isInstanceOf(LazotepConvert.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void defeatedSiegeMayBeLeftInExileInsteadOfCastingItsBackFace() {
        Permanent battle = addBattleWithNoDefenseCounters();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBattleWithNoDefenseCounters() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfAmonkhet());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        return battle;
    }

    private void defeatBattle(Permanent battle) {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
