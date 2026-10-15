package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MothersYamazaki.class, MothriderSamurai.class, GrizzlyBears.class, Forest.class, TurnToFrog.class})
class MothersYamazakiTest extends BaseCardTest {

    @Test
    @DisplayName("Two Mothers Yamazaki boost Samurai you control, including themselves")
    void exactlyTwoCopiesBoostSamurai() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        Permanent samurai = harness.addToBattlefieldAndReturn(player1, new MothriderSamurai());
        Permanent nonSamurai = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int firstPower = gqs.getEffectivePower(gd, first);
        int firstToughness = gqs.getEffectiveToughness(gd, first);
        int samuraiPower = gqs.getEffectivePower(gd, samurai);
        int samuraiToughness = gqs.getEffectiveToughness(gd, samurai);
        int nonSamuraiPower = gqs.getEffectivePower(gd, nonSamurai);
        int nonSamuraiToughness = gqs.getEffectiveToughness(gd, nonSamurai);

        Permanent second = harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(firstPower + 4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(firstToughness + 4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(firstPower + 4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(firstToughness + 4);
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(samuraiPower + 4);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(samuraiToughness + 4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonSamurai)).isEqualTo(nonSamuraiPower);
        assertThat(gqs.getEffectiveToughness(gd, nonSamurai)).isEqualTo(nonSamuraiToughness);
    }

    @Test
    @DisplayName("The legend exemption is counted separately for each controller")
    void legendExemptionUsesControllerCount() {
        harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        harness.addToBattlefieldAndReturn(player2, new MothersYamazaki());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A third copy under one controller restores the legend rule")
    void thirdCopyRestoresLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }

    @Test
    @DisplayName("Partner with itself lets a target player search for another copy")
    void partnerWithItselfSearchesForAnotherCopy() {
        Card copy = new MothersYamazaki();
        Forest decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, copy));
        harness.setHand(player1, List.of(new MothersYamazaki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mothers Yamazaki");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    void oneRemainingAbilityProtectsBothCopiesFromLegendRule() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, first.getId());

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, second);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
    }

    @Test
    void bonusDoesNotAffectOpponentsAndEndsWhenOnlyOneCopyRemains() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MothersYamazaki());
        int initialPower = gqs.getEffectivePower(gd, first);
        int opponentPower = gqs.getEffectivePower(gd, opponent);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MothersYamazaki());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(initialPower + 4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(opponentPower);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(initialPower);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card copy = new MothersYamazaki();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(copy));
        harness.setHand(player1, List.of(new MothersYamazaki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(copy);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void partnerSearchCanResolveWithoutMatchingCard() {
        Forest decoy = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy));
        harness.setHand(player1, List.of(new MothersYamazaki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
        assertThat(gd.stack).isEmpty();
    }
}
