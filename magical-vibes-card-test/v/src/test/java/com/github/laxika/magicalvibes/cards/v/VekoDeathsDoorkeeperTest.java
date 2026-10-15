package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({VekoDeathsDoorkeeper.class, GrizzlyBears.class, Shock.class})
class VekoDeathsDoorkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature card with perpetual Spirit, 1/1, and {W/B} modifications")
    void returnsPerpetuallyModifiedCreatureCardAndAllowsAlternateCast() {
        Permanent veko = addReadyVeko();
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        activate(veko, target);

        Card modified = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(modified).isNotSameAs(target);
        assertThat(modified.getId()).isEqualTo(target.getId());
        assertThat(modified.getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(modified.getPower()).isEqualTo(1);
        assertThat(modified.getToughness()).isEqualTo(1);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId())
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Only a creature card in the controller's graveyard is a legal target")
    void onlyOwnCreatureCardsAreTargetable() {
        Permanent veko = addReadyVeko();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card nonCreature = new Shock();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(veko);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a non-Spirit creature to sacrifice")
    void requiresNonSpiritCreatureForSacrifice() {
        Permanent veko = addReadyVeko();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(veko);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVeko() {
        Permanent veko = harness.addToBattlefieldAndReturn(player1, new VekoDeathsDoorkeeper());
        veko.setSummoningSick(false);
        return veko;
    }

    private void activate(Permanent veko, Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(veko);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
