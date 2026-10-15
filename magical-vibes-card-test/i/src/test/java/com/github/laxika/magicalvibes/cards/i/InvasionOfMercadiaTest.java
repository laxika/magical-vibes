package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KyrenFlamewright;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, IridescentBlademaster.class, InvasionOfMercadia.class,
        KyrenFlamewright.class, Mountain.class})
class InvasionOfMercadiaTest extends BaseCardTest {

    @Test
    void enteringMayDiscardToDrawTwo() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        Card discarded = new IridescentBlademaster();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new InvasionOfMercadia(), discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void defeatCastsKyrenFlamewrightTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfMercadia());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent flamewright = findPermanent(player1, "Kyren Flamewright");
        assertThat(flamewright.isTransformed()).isTrue();
    }

    @Test
    void flamewrightCreatesTokensBoostsOwnCreaturesAndGrantsHaste() {
        Permanent flamewright = addCreatureReady(player1, new KyrenFlamewright());
        Permanent blademaster = addCreatureReady(player1, new IridescentBlademaster());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectiveColors(gd, token))
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        });
        assertThat(gqs.getEffectivePower(gd, flamewright)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, flamewright, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, blademaster, Keyword.HASTE)).isTrue();
    }

    @Test
    void decliningDiscardLeavesHandAndLibraryUnchanged() {
        Card kept = new Forest();
        Card draw = new Mountain();
        harness.setHand(player1, List.of(new InvasionOfMercadia(), kept));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptingWithEmptyHandDoesNotDraw() {
        Card draw = new Forest();
        harness.setHand(player1, List.of(new InvasionOfMercadia()));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void defeatedSiegeMayRemainInExileInsteadOfBeingCast() {
        InvasionOfMercadia card = new InvasionOfMercadia();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, card);
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Kyren Flamewright");
    }

    @Test
    void flamewrightBonusesExcludeOpponentsAndLaterCreaturesAndExpire() {
        Permanent flamewright = addCreatureReady(player1, new KyrenFlamewright());
        Permanent own = addCreatureReady(player1, new IridescentBlademaster());
        Permanent opposing = addCreatureReady(player2, new IridescentBlademaster());
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(flamewright.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.HASTE)).isFalse();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new IridescentBlademaster());
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, later, Keyword.HASTE)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, flamewright)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, flamewright, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(2).allSatisfy(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
                });
    }
}
