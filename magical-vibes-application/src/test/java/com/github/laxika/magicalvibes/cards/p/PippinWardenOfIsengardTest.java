package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FeastingTrollKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GilraenDNedainProtector;
import com.github.laxika.magicalvibes.cards.m.MerryWardenOfIsengard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PippinWardenOfIsengard.class, FeastingTrollKing.class, GrizzlyBears.class,
        MerryWardenOfIsengard.class, GilraenDNedainProtector.class})
class PippinWardenOfIsengardTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Merry")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card merry = new MerryWardenOfIsengard();
        harness.setLibrary(player2, List.of(merry));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new PippinWardenOfIsengard());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(merry);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The first ability creates a Food token")
    void createsFoodToken() {
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, pippin), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(pippin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing four Foods boosts other creatures and gives them haste")
    void sacrificesFourFoodsForTeamBoostAndHaste() {
        createThreeFoods();
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, pippin), 0, null, null);
        harness.passBothPriorities();
        harness.performUntapStep(player1);

        assertThat(findPermanents(player1, "Food")).hasSize(4);

        harness.activateAbility(player1, indexOf(player1, pippin), 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pippin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pippin, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    private void createThreeFoods() {
        harness.castFromHand(player1, new FeastingTrollKing(), "{2}{G}{G}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card merry = new MerryWardenOfIsengard();
        harness.setLibrary(player2, List.of(merry));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new PippinWardenOfIsengard());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(merry);
    }

    @Test
    void cannotPayWithOnlyThreeFoods() {
        createThreeFoods();
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, pippin), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
        assertThat(pippin.isTapped()).isFalse();
    }

    @Test
    void boostCannotBeActivatedOutsideMainPhase() {
        Permanent pippin = prepareFourFoods();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, pippin), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(4);
        assertThat(pippin.isTapped()).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndLaterCreaturesAreNotBoosted() {
        Permanent pippin = prepareFourFoods();
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        harness.activateAbility(player1, indexOf(player1, pippin), 1, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, gilraen)).isEqualTo(2);
        harness.passBothPriorities();
        Permanent laterCreature = addCreatureReady(player1, new MerryWardenOfIsengard());

        assertThat(gqs.getEffectivePower(gd, gilraen)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, gilraen, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, gilraen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gilraen)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, gilraen, Keyword.HASTE)).isFalse();
    }

    @Test
    void returnedPippinIsAnotherCreatureForItsPendingAbility() {
        Permanent pippin = prepareFourFoods();
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        harness.activateAbility(player1, indexOf(player1, pippin), 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, gilraen), 0, null, pippin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Pippin, Warden of Isengard");
        assertThat(returned.getId()).isNotEqualTo(pippin.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
    }

    private Permanent prepareFourFoods() {
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());
        for (int i = 0; i < 4; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.activateAbility(player1, indexOf(player1, pippin), 0, null, null);
            harness.passBothPriorities();
            harness.performUntapStep(player1);
        }
        return pippin;
    }
}
