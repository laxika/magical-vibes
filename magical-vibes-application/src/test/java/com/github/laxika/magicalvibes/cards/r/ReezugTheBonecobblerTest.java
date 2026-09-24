package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ReezugTheBonecobbler.class, GrizzlyBears.class, Shock.class})
class ReezugTheBonecobblerTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually changes a creature in the graveyard to an artifact and allows it to be cast")
    void perpetuallyChangesCreatureToArtifactAndAllowsCastThisTurn() {
        Permanent reezug = addReadyReezug();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        activate(reezug, bears);

        Card modified = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(modified).isNotSameAs(bears);
        assertThat(modified.getId()).isEqualTo(bears.getId());
        assertThat(modified.hasType(CardType.ARTIFACT)).isTrue();
        assertThat(modified.hasType(CardType.CREATURE)).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, modified.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId())
                        && permanent.getCard().hasType(CardType.ARTIFACT)
                        && !permanent.getCard().hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard are legal targets")
    void onlyOwnCreatureCardsAreTargetable() {
        Permanent reezug = addReadyReezug();
        Card nonCreature = new Shock();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(reezug);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyReezug() {
        Permanent reezug = harness.addToBattlefieldAndReturn(player1, new ReezugTheBonecobbler());
        reezug.setSummoningSick(false);
        return reezug;
    }

    private void activate(Permanent reezug, Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(reezug);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
