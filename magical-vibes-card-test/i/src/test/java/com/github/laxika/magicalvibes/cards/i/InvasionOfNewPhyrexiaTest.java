package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TeferiAkosaOfZhalfir;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({Forest.class, GrizzlyBears.class, InvasionOfNewPhyrexia.class,
        Shock.class, TeferiAkosaOfZhalfir.class, YouthfulKnight.class})
class InvasionOfNewPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X 2/2 Knight tokens")
    void entersWithXKnightTokens() {
        castSiege(player1, 2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .hasSize(2);
    }

    @Test
    @DisplayName("+1 draws two cards and can discard a creature instead")
    void plusOneDrawsAndDiscardsCreature() {
        addReadyTeferi(player1, 4);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard).isNotNull();
        harness.handleCardChosen(player1, discard.validIndices().getFirst());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Teferi Akosa of Zhalfir")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 gives Knights +1/+0 and ward {1}")
    void minusTwoGrantsKnightBoostAndWard() {
        addReadyTeferi(player1, 4);
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);
    }

    @Test
    @DisplayName("-3 taps creatures and shuffles an opposing nonland within the tapped count")
    void minusThreeUsesTappedCreatureCountForTarget() {
        addReadyTeferi(player1, 4);
        Permanent firstCreature = addCreatureReady(player1, new YouthfulKnight());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent == target);
        assertThat(gd.playerDecks.get(player2.getId())).contains(target.getCard());
        assertThat(findPermanent(player1, "Teferi Akosa of Zhalfir")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TeferiAkosaOfZhalfir());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    @Test
    void zeroXCreatesNoKnights() {
        castSiege(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .isEmpty();
        assertThat(findPermanent(player1, "Invasion of New Phyrexia")
                .getCounterCount(CounterType.DEFENSE)).isEqualTo(6);
    }

    @Test
    void plusOneDiscardsTwoWhenThereIsNoCreature() {
        addReadyTeferi(player1, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void plusOneCanDeclineCreatureDiscardAndDiscardTwoOthers() {
        addReadyTeferi(player1, 4);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void minusThreeWithNoCreaturesStillShufflesAnOpposingToken() {
        castSiege(player2, 1);
        Permanent target = findPermanent(player2, "Knight");
        addReadyTeferi(player1, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void minusThreeChoosingZeroCreaturesStillShufflesAnOpposingToken() {
        castSiege(player2, 1);
        Permanent target = findPermanent(player2, "Knight");
        addReadyTeferi(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void emblemSurvivesTeferiAndOnlyBoostsYourKnights() {
        addReadyTeferi(player1, 2);
        Permanent ownKnight = addCreatureReady(player1, new YouthfulKnight());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKnight = addCreatureReady(player2, new YouthfulKnight());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Teferi Akosa of Zhalfir");
        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKnight)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingKnight)).isEqualTo(2);
        Permanent laterKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        assertThat(gqs.getEffectivePower(gd, laterKnight)).isEqualTo(3);
    }

    private void castSiege(Player player, int x) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new InvasionOfNewPhyrexia()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, x);
        gs.playCard(gd, player, 0, x, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void defeatingSiegeCastsTeferiWithFourLoyalty() {
        castSiege(player1, 0);
        Permanent siege = findPermanent(player1, "Invasion of New Phyrexia");
        assertThat(siege.getProtectorPlayerId()).isEqualTo(player2.getId());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castInstant(player1, 0, siege.getId());
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Invasion of New Phyrexia");
        assertThat(findPermanent(player1, "Teferi Akosa of Zhalfir")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .isEmpty();
    }

    @Test
    void payingWardAllowsOpposingSpellToResolve() {
        addReadyTeferi(player1, 4);
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void minusThreeOnlyOffersUntappedOwnCreaturesAndTargetsWithinCount() {
        castSiege(player2, 1);
        Permanent token = findPermanent(player2, "Knight");
        Permanent expensiveTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addReadyTeferi(player1, 4);
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        tapped.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice tapChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(tapChoice).isNotNull();
        assertThat(tapChoice.validIds()).containsExactly(untapped.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(untapped.getId()));
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(token.getId())
                .doesNotContain(expensiveTarget.getId(), land.getId(), untapped.getId(), tapped.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(untapped.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }
}
