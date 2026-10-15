package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValkiGodOfLies.class, TibaltCosmicImpostor.class, Forest.class, GrizzlyBears.class, MaskedVandal.class})
class ValkiGodOfLiesTest extends BaseCardTest {

    @Test
    void entersAndExilesAnOpponentCreatureUntilValkiLeaves() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest nonCreature = new Forest();
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        harness.setHand(player2, List.of(creature, nonCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent valki = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(valki.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nonCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, valki));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nonCreature, creature);
        assertThat(gd.getCardsExiledByPermanent(valki.getId())).isEmpty();
    }

    @Test
    void becomesACopyOfAQualifyingExiledCreature() {
        Permanent valki = harness.addToBattlefieldAndReturn(player1, new ValkiGodOfLies());
        GrizzlyBears exiledCreature = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCreature, valki.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int valkiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(valki);
        harness.activateAbility(player1, valkiIndex, 0, 2, null);
        harness.passBothPriorities();

        assertThat(valki.getCard().getName()).isEqualTo(exiledCreature.getName());
        assertThat(gqs.getEffectivePower(gd, valki)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valki)).isEqualTo(2);
    }

    @Test
    void TibaltCreatesThePlayPermissionEmblemAndTracksEachLibraryCard() {
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        Forest playerOneTop = new Forest();
        GrizzlyBears playerTwoTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(playerOneTop));
        harness.setLibrary(player2, List.of(playerTwoTop));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tibalt = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);

        int tibaltIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tibalt);
        harness.activateAbility(player1, tibaltIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(tibalt.getId()))
                .containsExactly(playerOneTop, playerTwoTop);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, tibalt));

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, playerTwoTop.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, playerTwoTop.getName());
    }

    @Test
    void tibaltEmblemExistsAsSoonAsTheSpellResolves() {
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tibaltMinusThreeAllowsCastingTheExiledCreatureAfterTibaltLeaves() {
        Permanent tibalt = harness.addToBattlefieldAndReturn(player1, new TibaltCosmicImpostor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaskedVandal());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, tibalt));

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, target.getCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Masked Vandal");
    }

    @Test
    void tibaltUltimateAllowsCastingCardsFromBothGraveyardsAfterHeDies() {
        Permanent tibalt = harness.addToBattlefieldAndReturn(player1, new TibaltCosmicImpostor());
        tibalt.setCounterCount(CounterType.LOYALTY, 8);
        MaskedVandal ownCard = new MaskedVandal();
        MaskedVandal opponentCard = new MaskedVandal();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tibalt, Cosmic Impostor");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(ownCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentCard.getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, opponentCard.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, ownCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void choosingBetweenQualifyingCardsCopiesWithoutMovingEitherOutOfExile() {
        Permanent valki = harness.addToBattlefieldAndReturn(player1, new ValkiGodOfLies());
        MaskedVandal first = new MaskedVandal();
        MaskedVandal second = new MaskedVandal();
        gd.addToExile(player2.getId(), first, valki.getId());
        gd.addToExile(player2.getId(), second, valki.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(valki.getCard().getName()).isEqualTo("Masked Vandal");
        assertThat(gqs.getEffectivePower(gd, valki)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, valki)).isEqualTo(3);
        assertThat(gd.getCardsExiledByPermanent(valki.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(valki);
    }

    @Test
    void activatingWithTheWrongManaValueDoesNotCopyTheExiledCreature() {
        Permanent valki = harness.addToBattlefieldAndReturn(player1, new ValkiGodOfLies());
        MaskedVandal creature = new MaskedVandal();
        gd.addToExile(player2.getId(), creature, valki.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(valki.getCard().getName()).isEqualTo("Valki, God of Lies");
        assertThat(gd.getCardsExiledByPermanent(valki.getId())).containsExactly(creature);
    }

    @Test
    void exiledCreatureReturnsEvenAfterValkiBecomesItsCopy() {
        MaskedVandal creature = new MaskedVandal();
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        harness.setHand(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        Permanent valki = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        assertThat(valki.getCard().getName()).isEqualTo("Masked Vandal");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, valki));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        harness.assertInHand(player1, "Valki, God of Lies");
    }

    @Test
    void leavingBeforeTheEnterTriggerResolvesStillRevealsTheOpponentsHand() throws Exception {
        MaskedVandal creature = new MaskedVandal();
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        harness.setHand(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent valki = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, valki));

        List<GameEventEnvelope> events = new ArrayList<>();
        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.passBothPriorities();
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.subjectPlayerId().equals(player2.getId())
                        && reveal.zone() == GameEventFact.RevealZone.HAND)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::name)
                            .containsExactly("Masked Vandal");
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

}
