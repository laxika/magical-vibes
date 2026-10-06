package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatchetFieldMedic.class, RatchetRescueRacer.class,
        MishrasBauble.class, MindStone.class, SoulWarden.class})
class RatchetFieldMedicTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsRatchetConvertedWithLivingMetal() {
        Permanent ratchet = castRatchetConverted();

        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(ratchet.getCard()).isInstanceOf(RatchetRescueRacer.class);
        assertThat(gqs.isCreature(gd, ratchet)).isTrue();
        assertThat(gqs.isArtifact(ratchet)).isTrue();
    }

    @Test
    void lifeGainMayConvertRatchetAndReturnTappedArtifactWithinLifeCap() {
        harness.addToBattlefield(player1, new SoulWarden());
        MishrasBauble bauble = new MishrasBauble();
        harness.setGraveyard(player1, List.of(bauble));
        Permanent ratchet = harness.enterBattlefieldAndReturn(player1, new RatchetFieldMedic());

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bauble.getId()));
        harness.passBothPriorities();

        assertThat(ratchet.isTransformed()).isTrue();
        Permanent returned = findPermanent(player1, "Mishra's Bauble");
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void nontokenArtifactLeavingBattlefieldConvertsRatchetOnce() {
        Permanent ratchet = castRatchetConverted();
        harness.addToBattlefield(player1, new MishrasBauble());

        harness.activateAbility(player1, 1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isFalse();
        assertThat(ratchet.getCard()).isInstanceOf(RatchetFieldMedic.class);
    }

    @Test
    void tokenArtifactLeavingBattlefieldDoesNotConvertRatchet() {
        Permanent ratchet = castRatchetConverted();
        MishrasBauble tokenBauble = new MishrasBauble();
        tokenBauble.setToken(true);
        harness.addToBattlefield(player1, tokenBauble);

        harness.activateAbility(player1, 1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(ratchet.getCard()).isInstanceOf(RatchetRescueRacer.class);
    }

    @Test
    void livingMetalDoesNotMakeRatchetACreatureOnOpponentsTurn() {
        Permanent ratchet = castRatchetConverted();

        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, ratchet)).isFalse();
        assertThat(gqs.isArtifact(ratchet)).isTrue();
    }

    @Test
    void decliningConversionLeavesArtifactInGraveyard() {
        harness.addToBattlefield(player1, new SoulWarden());
        MishrasBauble bauble = new MishrasBauble();
        harness.setGraveyard(player1, List.of(bauble));
        Permanent ratchet = harness.enterBattlefieldAndReturn(player1, new RatchetFieldMedic());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bauble);
        assertThat(countPermanents(player1, "Mishra's Bauble")).isZero();
    }

    @Test
    void conversionIsAllowedWithoutAnyArtifactToReturn() {
        harness.addToBattlefield(player1, new SoulWarden());
        Permanent ratchet = harness.enterBattlefieldAndReturn(player1, new RatchetFieldMedic());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnTargetsExcludeNonartifactsOverCapAndOpponentsCards() {
        harness.addToBattlefield(player1, new SoulWarden());
        MishrasBauble bauble = new MishrasBauble();
        MindStone overCap = new MindStone();
        SoulWarden nonartifact = new SoulWarden();
        MishrasBauble opponentsBauble = new MishrasBauble();
        harness.setGraveyard(player1, List.of(bauble, overCap, nonartifact));
        harness.setGraveyard(player2, List.of(opponentsBauble));
        harness.enterBattlefieldAndReturn(player1, new RatchetFieldMedic());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(bauble);
        harness.handleMultipleCardsChosen(player1, List.of(bauble.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(overCap, nonartifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsBauble);
    }

    @Test
    void returnCapIncludesLifeGainedEarlierThisTurn() {
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addToBattlefield(player1, new SoulWarden());
        MindStone stone = new MindStone();
        harness.setGraveyard(player1, List.of(stone));
        harness.enterBattlefieldAndReturn(player1, new RatchetFieldMedic());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(stone.getId()));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mind Stone").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(stone);
    }

    @Test
    void opponentsNontokenArtifactDoesNotConvertRatchet() {
        Permanent ratchet = castRatchetConverted();
        harness.addToBattlefield(player2, new MishrasBauble());

        harness.activateAbility(player2, 0, 0, player1.getId());
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isTrue();
    }

    @Test
    void backFaceDoesNotTriggerAgainAfterConvertingAwayAndBackInSameTurn() {
        Permanent ratchet = castRatchetConverted();
        harness.addToBattlefield(player1, new MishrasBauble());
        harness.activateAbility(player1, 1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(ratchet.isTransformed()).isFalse();

        harness.addToBattlefield(player1, new SoulWarden());
        harness.enterBattlefieldAndReturn(player1, new SoulWarden());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        resolveAllTriggers();
        assertThat(ratchet.isTransformed()).isTrue();

        harness.addToBattlefield(player1, new MishrasBauble());
        harness.activateAbility(player1, 4, 0, player2.getId());
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isTrue();
    }

    @Test
    void multiplePendingLifeGainTriggersConvertRatchetOnlyOnce() {
        Permanent ratchet = harness.addToBattlefieldAndReturn(player1, new RatchetFieldMedic());
        harness.inMutationScope(() -> {
            harness.setLife(player1, 22);
            gd.lifeGainedThisTurn.put(player1.getId(), 2);
            harness.getTriggerCollectionService().checkLifeGainTriggers(gd, player1.getId(), 1);
            harness.getTriggerCollectionService().checkLifeGainTriggers(gd, player1.getId(), 1);
        });
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(ratchet.isTransformed()).isTrue();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void normalCastEntersOnFrontFaceAndLifelinkTriggersOptionalConversion() {
        harness.castFromHand(player1, new RatchetFieldMedic(), "{2}{W}");
        resolveAllTriggers();
        Permanent ratchet = findPermanent(player1, "Ratchet, Field Medic");
        assertThat(ratchet.isTransformed()).isFalse();
        ratchet.setSummoningSick(false);
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(ratchet.isTransformed()).isFalse();
    }

    @Test
    void convertedRatchetHasLifelinkWithoutFrontFaceLifeGainTrigger() {
        Permanent ratchet = castRatchetConverted();
        ratchet.setSummoningSick(false);
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castRatchetConverted() {
        harness.setHand(player1, List.of(new RatchetFieldMedic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        return findPermanent(player1, "Ratchet, Rescue Racer");
    }
}
