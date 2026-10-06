package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LashOut;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RebellionOfTheFlamekin.class, WoodlandChangeling.class, Forest.class, LashOut.class})
class RebellionOfTheFlamekinTest extends BaseCardTest {

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental Shaman"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Won clash: paying {1} creates a 3/1 Elemental Shaman with haste")
    void wonClashCreatesHasteToken() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        // Higher mana value on top for player1 (WoodlandChangeling MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new WoodlandChangeling());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities(); // resolve clash trigger → may-pay prompt
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = token();
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Lost clash: paying {1} creates the token but it has no haste")
    void lostClashCreatesTokenWithoutHaste() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        // Lower mana value on top for player1 (Forest MV 0 < WoodlandChangeling MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new WoodlandChangeling());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = token();
        assertThat(token).isNotNull();
        assertThat(token.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining the {1} payment creates no token")
    void decliningCreatesNoToken() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        gd.playerDecks.get(player1.getId()).addFirst(new WoodlandChangeling());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(token()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste granted on a won clash wears off at end of turn")
    void hasteWearsOffAtCleanup() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        gd.playerDecks.get(player1.getId()).addFirst(new WoodlandChangeling());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(token().hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token().hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void tiedClashCreatesTokenWithoutHaste() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setLibrary(player2, List.of(new WoodlandChangeling()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(token()).isNotNull();
        assertThat(token().hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void unableToPayCreatesNoToken() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Elemental Shaman");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void winningClashTokenKeepsHasteWhenRevealedCardIsBottomed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new LashOut()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new WoodlandChangeling(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Woodland Changeling"));
        assertThat(token()).isNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(token()).isNull();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(token()).isNotNull();
        assertThat(token().hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningPaymentKeepsManaInPool() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Elemental Shaman");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void winningOpponentsClashCreatesHasteToken() {
        prepareOpponentsClash();
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setLibrary(player2, List.of(new Forest()));

        resolveOpponentsClash();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(token()).isNotNull();
        assertThat(token().hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void losingOpponentsClashStillCreatesToken() {
        prepareOpponentsClash();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new WoodlandChangeling()));

        resolveOpponentsClash();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(token()).isNotNull();
        assertThat(token().hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void tyingOpponentsClashStillCreatesToken() {
        prepareOpponentsClash();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        resolveOpponentsClash();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(token()).isNotNull();
        assertThat(token().hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void prepareOpponentsClash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player2, List.of(new LashOut()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void resolveOpponentsClash() {
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player2, "Woodland Changeling"));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
