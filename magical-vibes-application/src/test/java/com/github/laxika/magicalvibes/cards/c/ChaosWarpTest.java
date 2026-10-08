package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BattleScreech;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosWarp.class, CopyEnchantment.class, BattleScreech.class, FountainOfYouth.class, GrizzlyBears.class, Pacifism.class, PsychogenicProbe.class, Shock.class})
class ChaosWarpTest extends BaseCardTest {

    @Test
    void simulationAnswersPreparedCopiedAuraIndependentlyOfLiveGame() {
        Permanent firstHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent originalAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        originalAura.setAttachedTo(firstHost.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        target.setExileIfLeavesBattlefield(true);
        CopyEnchantment copyCard = new CopyEnchantment();
        harness.setLibrary(player2, List.of(copyCard));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, originalAura.getId());
        PendingInteraction liveChoice = gd.interaction.activeInteraction();
        assertThat(liveChoice).isInstanceOf(PendingInteraction.PermanentChoice.class);
        GameData simulation = gd.simulationCopy();

        gs.handleInteractionAnswer(simulation, player2, new InteractionAnswer.PermanentChosen(firstHost.getId()));

        assertThat(gd.interaction.activeInteraction()).isSameAs(liveChoice);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(copyCard);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == copyCard);
        harness.handlePermanentChosen(player2, secondHost.getId());

        Permanent simulatedAura = simulation.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == copyCard).findFirst().orElseThrow();
        Permanent liveAura = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == copyCard).findFirst().orElseThrow();
        assertThat(simulatedAura.getAttachedTo()).isEqualTo(firstHost.getId());
        assertThat(liveAura.getAttachedTo()).isEqualTo(secondHost.getId());
    }

    @Test
    void shufflesTargetPermanentThenPutsPermanentTopCardOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void leavesRevealedNonPermanentCardInLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Shock shock = new Shock();
        harness.setLibrary(player2, List.of(shock));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player2.getId())).contains(shock);
        harness.assertNotInGraveyard(player2, "Shock");
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesOwnerLibraryRatherThanCurrentControllerLibrary() {
        FountainOfYouth card = new FountainOfYouth();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        harness.setLibrary(player2, List.of());
        Shock untouched = new Shock();
        harness.setLibrary(player1, List.of(untouched));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock untouched = new Shock();
        harness.setLibrary(player2, List.of(untouched));
        harness.setHand(player1, List.of(new ChaosWarp(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNotReturnShuffledTokenToBattlefield() {
        harness.castFromHand(player1, new BattleScreech(), "{2}{W}{W}");
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void revealedAuraOffersOwnerAnAttachmentChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        target.setAttachedTo(creature.getId());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, creature.getId());

        Permanent aura = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Pacifism)
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void leavesRevealedAuraInLibraryWhenNothingCanBeEnchanted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setExileIfLeavesBattlefield(true);
        Pacifism aura = new Pacifism();
        harness.setLibrary(player2, List.of(aura));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertNotInGraveyard(player2, "Pacifism");
    }

    @Test
    void shufflesEvenWhenTargetIsExiledInsteadOfEnteringLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setExileIfLeavesBattlefield(true);
        Shock revealed = new Shock();
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(revealed);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertLife(player2, 18);
    }

}
