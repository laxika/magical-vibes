package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhoenixOfIteration.class, Hurricane.class, Shock.class})
class PhoenixOfIterationTest extends BaseCardTest {

    @Test
    void cheapInstantPerpetuallyBoostsPhoenixWithoutReturningIt() {
        Permanent phoenix = addReadyPhoenix(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phoenix);
        assertThat(gqs.getEffectivePower(gd, phoenix)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, phoenix)).isEqualTo(2);
    }

    @Test
    void fiveManaSpellMayExileAndReturnBattlefieldPhoenixTapped() {
        addReadyPhoenix(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Phoenix of Iteration");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void fiveManaSpellFromGraveyardMayReturnPhoenixTappedWithPerpetualBoost() {
        PhoenixOfIteration phoenix = new PhoenixOfIteration();
        harness.setGraveyard(player1, List.of(phoenix));
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Phoenix of Iteration");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void decliningReturnKeepsBattlefieldPhoenixUntappedAndBoosted() {
        Permanent phoenix = addReadyPhoenix(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castAndResolveSorcery(player1, 0, 4);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phoenix);
        assertThat(phoenix.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, phoenix)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, phoenix)).isEqualTo(2);
    }

    @Test
    void fourManaSpellDoesNotOfferReturn() {
        harness.setGraveyard(player1, List.of(new PhoenixOfIteration()));
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Phoenix of Iteration");
        harness.assertNotOnBattlefield(player1, "Phoenix of Iteration");
    }

    @Test
    void graveyardBoostsAccumulateAndSurviveReturn() {
        harness.setGraveyard(player1, List.of(new PhoenixOfIteration()));
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castAndResolveSorcery(player1, 0, 4);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Phoenix of Iteration");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    void decliningGraveyardReturnPreservesBoostForLaterTrigger() {
        harness.setGraveyard(player1, List.of(new PhoenixOfIteration()));
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 4);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Phoenix of Iteration");
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castAndResolveSorcery(player1, 0, 4);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Phoenix of Iteration");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    void opponentsInstantDoesNotBoostPhoenix() {
        Permanent phoenix = addReadyPhoenix(player1);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, phoenix)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phoenix)).isEqualTo(1);
    }

    @Test
    void creatureSpellDoesNotBoostPhoenix() {
        Permanent phoenix = addReadyPhoenix(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new PhoenixOfIteration()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, phoenix)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phoenix)).isEqualTo(1);
    }

    @Test
    @CardUsed(TormodsCrypt.class)
    void graveyardPhoenixExiledInResponseCannotReturnFromUnrelatedExile() {
        PhoenixOfIteration phoenix = new PhoenixOfIteration();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.addToBattlefield(player2, new TormodsCrypt());
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Phoenix of Iteration");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
    }

    private Permanent addReadyPhoenix(Player player) {
        return addCreatureReady(player, new PhoenixOfIteration());
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
