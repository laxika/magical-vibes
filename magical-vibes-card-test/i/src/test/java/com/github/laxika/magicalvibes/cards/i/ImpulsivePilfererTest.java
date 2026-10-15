package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpulsivePilferer.class, DoublingSeason.class})
class ImpulsivePilfererTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, Impulsive Pilferer creates a Treasure")
    void deathCreatesTreasure() {
        Permanent pilferer = addCreatureReady(player1, new ImpulsivePilferer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pilferer));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE));
    }

    @Test
    @DisplayName("Encore creates an untapped hasty copy that can attack later")
    void encoreCreatesUntappedHastyTokenCopy() {
        ImpulsivePilferer pilferer = new ImpulsivePilferer();
        harness.setGraveyard(player1, List.of(pilferer));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Impulsive Pilferer");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Impulsive Pilferer"));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Encore sacrifices its token copies at the next end step")
    void encoreSacrificesTokenCopyAtNextEndStep() {
        ImpulsivePilferer pilferer = new ImpulsivePilferer();
        harness.setGraveyard(player1, List.of(pilferer));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Impulsive Pilferer")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Impulsive Pilferer")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .hasSize(1);
    }

    @Test
    void encoreExilesSourceAsCostBeforeResolving() {
        harness.setGraveyard(player1, List.of(new ImpulsivePilferer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Impulsive Pilferer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Impulsive Pilferer");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void encoreCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new ImpulsivePilferer()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Impulsive Pilferer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void encoreCopyMustAttackIfAble() {
        harness.setGraveyard(player1, List.of(new ImpulsivePilferer()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void encoreSacrificesAllDoubledCopiesWithOneDelayedTrigger() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setGraveyard(player1, List.of(new ImpulsivePilferer()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Impulsive Pilferer")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Impulsive Pilferer")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .hasSize(4);
    }
}
