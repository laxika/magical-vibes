package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForumFamiliar.class, GrizzlyBears.class, Murder.class, Plains.class})
class ForumFamiliarTest extends BaseCardTest {

    @Test
    void turningFaceUpReturnsAnotherPermanentYouControlAndPutsCounterOnIt() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForumFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Forum Familiar");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(familiar));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownPermanent.getId())
                .doesNotContain(familiar.getId(), opposingPermanent.getId());
        harness.handlePermanentChosen(player1, ownPermanent.getId());
        harness.passBothPriorities();

        assertThat(familiar.isFaceDown()).isFalse();
        assertThat(familiar.getEffectivePower()).isEqualTo(2);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void turningFaceUpWithoutAnotherPermanentDoesNotCreateAResolution() {
        harness.setHand(player1, List.of(new ForumFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Forum Familiar");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(familiar));

        assertThat(familiar.isFaceDown()).isFalse();
        assertThat(familiar.getEffectivePower()).isEqualTo(1);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpDoesNotReturnAPermanentOrAddACounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new ForumFamiliar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Forum Familiar");
        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingTheTargetBeforeResolutionPreventsTheCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ForumFamiliar());
        Permanent familiar = castDisguisedFamiliar();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(familiar));
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(familiar).doesNotContain(target);
        harness.assertInGraveyard(player1, "Forum Familiar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingTheSourceDoesNotPreventReturningTheTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent familiar = castDisguisedFamiliar();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(familiar));
        harness.handlePermanentChosen(player1, land.getId());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Forum Familiar");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land, familiar);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningDisguiseWardCountersTheOpponentsSpell() {
        Permanent familiar = castDisguisedFamiliar();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(familiar);
        assertThat(familiar.isFaceDown()).isTrue();
        harness.assertInGraveyard(player2, "Murder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingTwoManaForDisguiseWardLetsTheSpellResolve() {
        Permanent familiar = castDisguisedFamiliar();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
        harness.assertInGraveyard(player1, "Forum Familiar");
        harness.assertInGraveyard(player2, "Murder");
        assertThat(gd.stack).isEmpty();
    }
    private Permanent castDisguisedFamiliar() {
        harness.setHand(player1, List.of(new ForumFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst().orElseThrow();
    }
}
