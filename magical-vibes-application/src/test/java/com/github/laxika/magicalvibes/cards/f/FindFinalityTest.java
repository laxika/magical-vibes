package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({FindFinality.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, HitchclawRecluse.class})
class FindFinalityTest extends BaseCardTest {

    private static final int FIND = 0;
    private static final int FINALITY = 1;

    @Test
    @DisplayName("Find returns up to two target creature cards from the graveyard")
    void findReturnsTwoCreatureCards() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(first, second, third, land));
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, FIND, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second).doesNotContain(third, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third, land);
    }

    @Test
    @DisplayName("Finality optionally adds counters before shrinking every creature")
    void finalityAddsCountersAndShrinksAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castFinality();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Finality still shrinks creatures when its counter choice is declined")
    void finalityMayBeDeclined() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castFinality();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Finality's creature shrink wears off at end of turn")
    void finalityShrinkWearsOff() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        castFinality();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(10);
    }

    @Test
    @DisplayName("Find can choose no targets even when creature cards are available")
    void findCanChooseZeroTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalSorcery(player1, 0, FIND, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Find can be cast with an empty graveyard")
    void findWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, FIND, List.of());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiGraveyardChoice) {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Find // Finality");
    }

    @Test
    @DisplayName("Find returns a single selected card and excludes the opponent's graveyard")
    void findReturnsOneCardFromOwnGraveyard() {
        Card own = new GrizzlyBears();
        Card other = new LlanowarElves();
        Card opposing = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(own, other));
        harness.setGraveyard(player2, List.of(opposing));
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, FIND, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(own.getId(), other.getId());
        harness.handleMultipleCardsChosen(player1, List.of(own.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(own).doesNotContain(other, opposing);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(own);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposing);
    }

    @Test
    @DisplayName("Find still returns its remaining legal target when another leaves the graveyard")
    void findReturnsRemainingLegalTarget() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, FIND, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(first);
        gd.exiledCards.add(new ExiledCardEntry(first, player1.getId(), null));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).contains(first);
    }

    @Test
    @DisplayName("Finality chooses only an owned creature and continues after the counter choice")
    void finalityChoosesAmongOwnCreatures() {
        Permanent first = addCreatureReady(player1, new AvatarOfMight());
        Permanent second = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposing = addCreatureReady(player2, new AvatarOfMight());
        castFinality();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);
    }

    @Test
    @DisplayName("Finality kills small creatures even when its controller has no creatures")
    void finalityWithoutOwnCreatures() {
        Card opposingCard = new GrizzlyBears();
        Permanent opposing = addCreatureReady(player2, opposingCard);
        castFinality();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finality does not shrink creatures entering later in the turn")
    void finalityDoesNotAffectLaterCreatures() {
        Permanent existing = addCreatureReady(player1, new AvatarOfMight());
        castFinality();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(4);
    }

    @Test
    @DisplayName("Finality's counters let a creature survive the subsequent shrink")
    void finalityCountersAllowCreatureToSurvive() {
        Permanent own = addCreatureReady(player1, new HitchclawRecluse());
        Card opposingCard = new HitchclawRecluse();
        Permanent opposing = addCreatureReady(player2, opposingCard);
        castFinality();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCard);
    }

    private void castFinality() {
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, FINALITY, List.of());
    }
}
