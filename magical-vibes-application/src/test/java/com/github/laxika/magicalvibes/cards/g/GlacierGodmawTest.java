package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacierGodmaw.class, Forest.class, GrizzlyBears.class})
class GlacierGodmawTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Lander token when Glacier Godmaw enters")
    void createsLanderOnEnter() {
        harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Landfall boosts all creatures you control and grants vigilance and haste until end of turn")
    void landfallBoostsOwnCreaturesUntilEndOfTurn() {
        Permanent godmaw = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void landerPaysTwoManaAndSacrificesToFindABasicLandTappedAndTriggerLandfall() {
        Permanent godmaw = harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        Forest forest = new Forest();
        GlacierGodmaw nonland = new GlacierGodmaw();
        harness.setLibrary(player1, List.of(nonland, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isTrue();
    }

    @Test
    void landerCanFailToFindABasicLandAndStillShuffles() {
        harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void landerSearchWithNoBasicLandsStillShufflesWithoutTriggeringLandfall() {
        Permanent godmaw = harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        GlacierGodmaw nonland = new GlacierGodmaw();
        harness.setLibrary(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isFalse();
    }

    @Test
    void landerCannotActivateWithOnlyOneMana() {
        harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
        assertThat(lander.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLanderCannotActivate() {
        harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        lander.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentLandDoesNotTriggerLandfall() {
        Permanent godmaw = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isFalse();
    }

    @Test
    void multipleLandEntriesStackTheirBoosts() {
        Permanent godmaw = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isTrue();
    }

    @Test
    void landfallAffectsCreaturesPresentAtResolutionButNotThoseEnteringLater() {
        harness.addToBattlefield(player1, new GlacierGodmaw());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());

        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }
}
