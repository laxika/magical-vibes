package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosIsMyPlaything.class, GiantGrowth.class, GrizzlyBears.class, Pacifism.class, Clone.class})
class ChaosIsMyPlaythingTest extends BaseCardTest {

    @Test
    void exilesOneOpponentPermanentAndEachPlayerPutsAPermanentFromTheirLibraryOntoTheBattlefield() {
        Permanent exiled = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card player1NonPermanent = new GiantGrowth();
        Card player1Permanent = new GrizzlyBears();
        Card player2NonPermanent = new GiantGrowth();
        Card player2Permanent = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1NonPermanent, player1Permanent));
        harness.setLibrary(player2, List.of(player2NonPermanent, player2Permanent));
        ChaosIsMyPlaything scheme = new ChaosIsMyPlaything();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL),
                (UUID) null,
                List.of(exiled.getId())));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiled.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == player1Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Permanent);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(player1NonPermanent);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(player2NonPermanent);
    }

    @Test
    void cannotTargetAPermanentYouControl() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ChaosIsMyPlaything scheme = new ChaosIsMyPlaything();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL),
                (UUID) null,
                List.of(ownPermanent.getId())));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownPermanent);
    }

    @Test
    void auraRemainsInLibraryWhenNoCreatureCanBeEnchantedAfterExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card skipped = new GiantGrowth();
        Card aura = new Pacifism();
        Card unrevealed = new GiantGrowth();
        harness.setLibrary(player1, List.of(skipped, aura, unrevealed));
        harness.setLibrary(player2, List.of(new GiantGrowth()));

        resolveScheme(List.of(target.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura, unrevealed, skipped);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void libraryWithoutPermanentsRetainsAllItsCardsAndOtherPlayerStillGetsAPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new GiantGrowth();
        Card second = new GiantGrowth();
        Card opponentPermanent = new GrizzlyBears();
        Card unrevealed = new GiantGrowth();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentPermanent, unrevealed));

        resolveScheme(List.of(target.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentPermanent);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(unrevealed);
    }

    @Test
    void doesNotRevealCardsWhenTheOnlyTargetHasLeftTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card firstPermanent = new GrizzlyBears();
        Card secondPermanent = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstPermanent));
        harness.setLibrary(player2, List.of(secondPermanent));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        resolveScheme(List.of(target.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstPermanent);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondPermanent);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotChooseZeroTargetsWhenAnOpponentHasNoPermanent() {
        ChaosIsMyPlaything scheme = new ChaosIsMyPlaything();

        assertThatThrownBy(() -> harness.getTargetLegalityService().validateMultiSpellTargets(
                gd, scheme, List.of(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void auraCannotEnchantACreatureEnteringInTheSameBatch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        Card aura = new Pacifism();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(aura));

        resolveScheme(List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(aura);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(aura);
    }

    @Test
    void auraAttachmentChoiceFinishesBeforeRevealedRemainderIsReturned() {
        Permanent firstHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card skipped = new GiantGrowth();
        Card aura = new Pacifism();
        Card unrevealed = new GiantGrowth();
        harness.setLibrary(player1, List.of(skipped, aura, unrevealed));
        harness.setLibrary(player2, List.of(new GiantGrowth()));

        resolveScheme(List.of(target.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped, aura, unrevealed);
        harness.handlePermanentChosen(player1, firstHost.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == aura
                        && firstHost.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, skipped);
    }

    @Test
    void revealedCloneChoosesItsCopyBeforeTheBatchEnters() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card clone = new Clone();
        Card skipped = new GiantGrowth();
        harness.setLibrary(player1, List.of(skipped, clone));
        harness.setLibrary(player2, List.of(new GiantGrowth()));

        resolveScheme(List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, host.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == clone
                        && permanent.getCard().getName().equals(host.getCard().getName()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(clone);
    }

    private void resolveScheme(List<UUID> targetIds) {
        ChaosIsMyPlaything scheme = new ChaosIsMyPlaything();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL),
                (UUID) null,
                targetIds));
        harness.passBothPriorities();
    }
}
