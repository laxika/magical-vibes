package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmryLurkerOfTheLoch.class, Forest.class, Ornithopter.class})
class EmryLurkerOfTheLochTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces Emry's casting cost")
    void affinityForArtifactsReducesCastingCost() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Entering the battlefield mills four cards")
    void entersMillsFour() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can cast the targeted artifact from the graveyard this turn")
    void castsTargetedArtifactFromGraveyard() {
        Permanent emry = addReadyEmry();
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ornithopter)));

        activate(emry, ornithopter);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ornithopter);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == ornithopter);
    }

    @Test
    @DisplayName("Only artifact cards in the controller's own graveyard are legal targets")
    void onlyOwnArtifactsAreTargetable() {
        Permanent emry = addReadyEmry();
        Card nonArtifact = new Forest();
        Card opponentArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(emry);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, index, 0, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, index, 0, null, opponentArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEmry() {
        Permanent emry = harness.addToBattlefieldAndReturn(player1, new EmryLurkerOfTheLoch());
        emry.setSummoningSick(false);
        return emry;
    }

    private void activate(Permanent emry, Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(emry);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
