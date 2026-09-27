package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheMindStone;
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

@CardUsed({EdgarMasterMachinist.class, TheMindStone.class, DarksteelIngot.class,
        GrizzlyBears.class, DarksteelCitadel.class})
class EdgarMasterMachinistTest extends BaseCardTest {

    @Test
    @DisplayName("Casts one nonland artifact from the graveyard each turn, and it enters tapped")
    void castsArtifactFromGraveyardTapped() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        TheMindStone mindStone = new TheMindStone();
        harness.setGraveyard(player1, List.of(mindStone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent mindStonePermanent = findPermanentByCardId(mindStone.getId());
        assertThat(mindStonePermanent.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast nonartifact or artifact land cards from the graveyard")
    void rejectsNonArtifactAndArtifactLandCards() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelCitadel()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gets +X/+0 on attack for the greatest artifact mana value")
    void boostsByGreatestArtifactManaValue() {
        Permanent edgar = addCreatureReady(player1, new EdgarMasterMachinist());
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new TheMindStone());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(edgar.getPowerModifier()).isEqualTo(4);
        assertThat(edgar.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(edgar.getPowerModifier()).isZero();
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
