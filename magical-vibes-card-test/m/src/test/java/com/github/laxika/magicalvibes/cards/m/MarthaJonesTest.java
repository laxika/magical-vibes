package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RoryWilliams;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarthaJones.class, RoryWilliams.class})
class MarthaJonesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and investigates")
    void entersAndInvestigates() {
        harness.setHand(player1, List.of(new MarthaJones()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed to draw and make Martha unblockable")
    void investigatedClueHasWorkingDrawAbility() {
        harness.setHand(player1, List.of(new MarthaJones()));
        harness.setLibrary(player1, List.of(new RoryWilliams()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        sacrificeClue(findPermanent(player1, "Clue"), null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanent(player1, "Martha Jones").isCantBeBlocked()).isTrue();
        harness.assertInHand(player1, "Rory Williams");
    }

    @Test
    @DisplayName("Sacrificing a Clue makes Martha and another creature unblockable")
    void clueSacrificeMakesMarthaAndTargetUnblockable() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent target = addCreatureReady(player1, new RoryWilliams());
        Permanent clue = addClueToken(player1);

        sacrificeClue(clue, target);

        assertThat(martha.isCantBeBlocked()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Clue makes Martha unblockable without a target")
    void clueSacrificeWorksWithoutOtherCreature() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent clue = addClueToken(player1);

        sacrificeClue(clue, null);

        assertThat(martha.isCantBeBlocked()).isTrue();
    }

    private void sacrificeClue(Permanent clue, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();
        if (target != null && gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The other target creature may be controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent target = addCreatureReady(player2, new RoryWilliams());

        sacrificeClue(addClueToken(player1), target);

        assertThat(martha.isCantBeBlocked()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The controller can decline a target even when another creature is available")
    void canDeclineAvailableTarget() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent other = addCreatureReady(player1, new RoryWilliams());
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(martha.isCantBeBlocked()).isTrue();
        assertThat(other.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue does not trigger Martha")
    void opponentsClueDoesNotTrigger() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent clue = addClueToken(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(martha.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("If the chosen target leaves, the ability does not make Martha unblockable")
    void illegalOnlyTargetPreventsEntireAbilityResolving() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent target = addCreatureReady(player1, new RoryWilliams());
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        resolveAllTriggers();

        assertThat(martha.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Martha leaving does not stop the other creature from becoming unblockable")
    void abilityResolvesWithoutMartha() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent target = addCreatureReady(player1, new RoryWilliams());
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, martha);
        resolveAllTriggers();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Both unblockable effects expire at the end of the turn")
    void unblockableEffectsExpire() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent target = addCreatureReady(player1, new RoryWilliams());
        sacrificeClue(addClueToken(player1), target);
        assertThat(martha.isCantBeBlocked()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(martha.isCantBeBlocked()).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    private Permanent addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        return harness.addToBattlefieldAndReturn(player, clueCard);
    }
}
