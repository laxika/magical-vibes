package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ImpulsivePilferer.class)
class ImpulsivePilfererTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, Impulsive Pilferer creates a Treasure")
    void deathCreatesTreasure() {
        addCreatureReady(player1, new ImpulsivePilferer());

        harness.sacrificePermanent(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE));
    }

    @Test
    @DisplayName("Encore creates a hasty token copy attacking the opponent")
    void encoreCreatesHastyAttackingTokenCopy() {
        ImpulsivePilferer pilferer = new ImpulsivePilferer();
        harness.setGraveyard(player1, List.of(pilferer));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Impulsive Pilferer"))
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Impulsive Pilferer"));
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

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Impulsive Pilferer")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
