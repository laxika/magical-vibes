package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.e.EidolonOfBlossoms;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedressFate.class, DarksteelRelic.class, GloriousAnthem.class,
        GrizzlyBears.class, Mountain.class, Pacifism.class, EidolonOfBlossoms.class})
class RedressFateTest extends BaseCardTest {

    @Test
    void returnsArtifactsAndEnchantmentsFromYourGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card artifact = new DarksteelRelic();
        Card enchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        castRedressFate();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifact.getId()) || card.getId().equals(enchantment.getId()));
    }

    @Test
    void leavesOtherCardTypesAndOpponentCardsInGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new GrizzlyBears();
        Card land = new Mountain();
        Card opponentArtifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        castRedressFate();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentArtifact);
    }

    private void castRedressFate() {
        harness.castFromHand(player1, new RedressFate(), "{6}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    void resolvesWithAnEmptyGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of());

        castRedressFate();

        harness.assertInGraveyard(player1, "Redress Fate");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void firstDrawOffersMiracle() {
        harness.setLibrary(player1, List.of(new RedressFate()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInHand(player1, "Redress Fate");
    }

    @Test
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new RedressFate()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Redress Fate");
    }

    @Test
    void miracleCostReturnsPermanentsDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new RedressFate()));
        harness.setGraveyard(player1, List.of(new DarksteelRelic(), new GloriousAnthem()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Redress Fate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void returnedAuraChoosesAnExistingCreatureToEnchant() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Pacifism()));

        castRedressFate();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Pacifism"))
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(creature.getId()));
    }

    @Test
    void auraWithNoLegalAttachmentRemainsInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Pacifism()));

        castRedressFate();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returningEnchantmentsEnterSimultaneously() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.setGraveyard(player1, List.of(new GloriousAnthem(), new EidolonOfBlossoms()));

        castRedressFate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Eidolon of Blossoms");
    }
}
