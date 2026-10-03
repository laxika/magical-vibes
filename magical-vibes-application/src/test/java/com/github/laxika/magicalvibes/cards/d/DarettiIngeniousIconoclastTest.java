package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarettiIngeniousIconoclast.class, GrizzlyBears.class, Spellbook.class})
class DarettiIngeniousIconoclastTest extends BaseCardTest {

    @Test
    void plusOneCreatesDefenderConstruct() {
        Permanent daretti = addReadyDaretti(6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(construct.getCard().isToken()).isTrue();
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(construct.getCard().getPower()).isEqualTo(1);
        assertThat(construct.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, construct, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void minusOneSacrificesArtifactThenDestroysTarget() {
        Permanent daretti = addReadyDaretti(6);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void minusOneMayBeDeclined() {
        Permanent daretti = addReadyDaretti(6);
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void ultimateCopiesBattlefieldArtifactThreeTimes() {
        addReadyDaretti(6);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Spellbook")).isEqualTo(3);
    }

    @Test
    void ultimateCopiesArtifactFromAnyGraveyardThreeTimes() {
        addReadyDaretti(6);
        Spellbook target = new Spellbook();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Spellbook")).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
    }

    @Test
    void minusOneDestroysNoncreatureArtifact() {
        addReadyDaretti(3);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    void minusOneCanSacrificeItsOwnTarget() {
        addReadyDaretti(3);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusOneDoesNotDestroyWithoutAnArtifactToSacrifice() {
        addReadyDaretti(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateCopiesOpponentsArtifactUnderYourControl() {
        addReadyDaretti(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        target.tap();

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Spellbook")).isEqualTo(3);
        assertThat(countTokenCopies(player2, "Spellbook")).isZero();
        assertThat(findPermanents(player1, "Spellbook")).allMatch(permanent -> !permanent.isTapped());
        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Daretti, Ingenious Iconoclast");
    }

    @Test
    void ultimateCopiesConstructToken() {
        Permanent daretti = addReadyDaretti(6);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent construct = findPermanent(player1, "Construct");
        daretti.setLoyaltyActivationsThisTurn(0);

        harness.activateAbility(player1, 0, 2, null, construct.getId());
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Construct")).isEqualTo(4);
        assertThat(findPermanents(player1, "Construct")).allSatisfy(token -> {
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.DEFENDER)).isTrue();
        });
    }

    @Test
    void ultimateDoesNotFollowBattlefieldTargetIntoGraveyard() {
        addReadyDaretti(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Spellbook")).isZero();
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    void minusOneDoesNotOfferSacrificeWhenTargetIsGone() {
        addReadyDaretti(3);
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDaretti(int loyalty) {
        Permanent daretti = harness.addToBattlefieldAndReturn(player1, new DarettiIngeniousIconoclast());
        daretti.setCounterCount(CounterType.LOYALTY, loyalty);
        daretti.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return daretti;
    }

    private long countTokenCopies(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }
}
