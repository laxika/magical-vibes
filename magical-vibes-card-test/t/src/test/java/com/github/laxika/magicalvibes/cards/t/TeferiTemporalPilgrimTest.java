package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.a.Audacity;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.ScrapworkMutt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiTemporalPilgrim.class, ArgothianSprite.class, ScrapworkMutt.class,
        Forest.class, Audacity.class, PsychogenicProbe.class})
class TeferiTemporalPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Gains loyalty whenever its controller draws a card")
    void gainsLoyaltyOnControllerDraw() {
        Permanent teferi = addReadyTeferi(player1, 4);
        harness.setLibrary(player1, List.of(new ArgothianSprite()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("The -2 token gets a +1/+1 counter whenever its controller draws")
    void spiritGrowsOnControllerDraw() {
        Permanent teferi = addReadyTeferi(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        harness.setLibrary(player1, List.of(new ArgothianSprite()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ultimate lets the opponent choose one permanent, then shuffles their remaining nonlands")
    void ultimateReturnsChosenPermanentAndShufflesRemainingNonlands() {
        Permanent teferi = addReadyTeferi(player1, 12);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent shuffled = harness.addToBattlefieldAndReturn(player2, new ScrapworkMutt());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card libraryCard = new ArgothianSprite();
        harness.setLibrary(player2, List.of(libraryCard));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(chosen.getId(), shuffled.getId(), land.getId());

        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(chosen.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCard, shuffled.getCard());
    }

    @Test
    @DisplayName("The ultimate cannot target its controller")
    void ultimateRequiresOpponentTarget() {
        addReadyTeferi(player1, 12);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void zeroAbilityDrawsAndThenGainsLoyalty() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Card drawn = new ArgothianSprite();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void eachDrawTriggersTeferiAndSpiritSeparately() {
        Permanent teferi = addReadyTeferi(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ScrapworkMutt()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
    }

    @Test
    void opponentDrawDoesNotTriggerTeferiOrSpirit() {
        Permanent teferi = addReadyTeferi(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        harness.setLibrary(player2, List.of(new ArgothianSprite()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    void ultimateCanReturnALandAndLeavesOtherLandsAlone() {
        addReadyTeferi(player1, 13);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonland = harness.addToBattlefieldAndReturn(player2, new ScrapworkMutt());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.playerHands.get(player2.getId())).contains(chosen.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherLand);
        assertThat(gd.playerDecks.get(player2.getId())).contains(nonland.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownPermanent);
    }

    @Test
    void ultimateReturnsAndShufflesToOwnersZones() {
        addReadyTeferi(player1, 13);
        Card chosenCard = new ArgothianSprite();
        chosenCard.setOwnerId(player1.getId());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, chosenCard);
        Card shuffledCard = new ScrapworkMutt();
        shuffledCard.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, shuffledCard);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(shuffledCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(shuffledCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void ultimateShufflesAuraOnReturnedCreatureInsteadOfPuttingItInGraveyard() {
        addReadyTeferi(player1, 13);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Audacity());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, creature.getId());

        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).contains(aura.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(aura.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateResolvesAgainstOpponentWithNoPermanents() {
        addReadyTeferi(player1, 13);
        Card libraryCard = new ArgothianSprite();
        harness.setLibrary(player2, List.of(libraryCard));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spiritAttacksWithoutTapping() {
        addReadyTeferi(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        spirit.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spirit))));

        assertThat(spirit.isAttacking()).isTrue();
        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    void spiritContinuesGrowingAfterTeferiLeaves() {
        Permanent teferi = addReadyTeferi(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(teferi);
        harness.setLibrary(player1, List.of(new ArgothianSprite()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
    }

    @Test
    void ultimateShufflesEachOwnersLibraryOnceForMultipleNonlands() {
        addReadyTeferi(player1, 13);
        harness.addToBattlefield(player1, new PsychogenicProbe());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.addToBattlefield(player2, new ScrapworkMutt());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TeferiTemporalPilgrim());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
