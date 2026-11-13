package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OjerTaqDeepestFoundation;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaebloomTrick.class, GrizzlyBears.class, TrollAscetic.class, Unsummon.class, ParallelLives.class,
        DoublingSeason.class, OjerTaqDeepestFoundation.class, ChatterfangSquirrelGeneral.class})
class FaebloomTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two flying Faeries and then taps a target opponent creature")
    void createsFaeriesThenTapsOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(faerie -> {
                    assertThat(faerie.getCard().getColor()).isEqualTo(CardColor.BLUE);
                    assertThat(faerie.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
                    assertThat(faerie.getCard().getKeywords()).contains(Keyword.FLYING);
                });
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId());
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still creates Faeries when no opponent creature can be tapped")
    void createsFaeriesWithoutValidTapTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void canTargetAnAlreadyTappedOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokensRemainWhenTapTargetLeavesInResponse() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotOfferHexproofOpponentCreatureAsTarget() {
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new TrollAscetic());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(legalTarget.getId());
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        assertThat(legalTarget.isTapped()).isTrue();
    }

    @Test
    void createsTokensWithoutTargetChoiceWhenOnlyOpponentCreatureHasHexproof() {
        harness.addToBattlefield(player2, new TrollAscetic());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Under one token doubler the written two Faeries are made twice: \"when you do\" triggers twice")
    void oneDoublerTriggersTheTapAbilityTwice() {
        harness.addToBattlefield(player1, new ParallelLives());

        assertThat(castAndCountTapAbilities()).isEqualTo(2);
        assertThat(faeries()).hasSize(4);
    }

    @Test
    @DisplayName("Under two token doublers the eight Faeries trigger \"when you do\" four times")
    void twoDoublersTriggerTheTapAbilityFourTimes() {
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player1, new DoublingSeason());

        assertThat(castAndCountTapAbilities()).isEqualTo(4);
        assertThat(faeries()).hasSize(8);
    }

    @Test
    @DisplayName("Under a token tripler the six Faeries trigger \"when you do\" three times")
    void triplerTriggersTheTapAbilityThreeTimes() {
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());

        assertThat(castAndCountTapAbilities()).isEqualTo(3);
        assertThat(faeries()).hasSize(6);
    }

    @Test
    @DisplayName("Squirrels a replacement adds beside the Faeries don't make \"when you do\" trigger again")
    void addedTokensOfAnotherKindDoNotTriggerTheTapAbilityAgain() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());

        assertThat(castAndCountTapAbilities()).isEqualTo(1);
        assertThat(faeries()).hasSize(2);
    }

    /**
     * Casts Faebloom Trick with an opponent creature to tap, answers each "when you do" ability's target with it, and
     * returns how many of those abilities went on the stack.
     */
    private int castAndCountTapAbilities() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, opponentCreature.getId());
        }
        return (int) gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
    }

    private List<Permanent> faeries() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FAERIE))
                .toList();
    }
}
