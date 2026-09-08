package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantAdephage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdolOfEnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles qualifying creature cards and returns them to the graveyard when it leaves")
    void exilesQualifyingCreaturesUntilItLeaves() {
        Card cheapCreature = new GrizzlyBears();
        Card expensiveCreature = new GiantAdephage();
        Card nonCreature = new Shock();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(cheapCreature, expensiveCreature, nonCreature));
        harness.setHand(player1, List.of(new IdolOfEndurance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent idol = findPermanent(player1, "Idol of Endurance");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(cheapCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expensiveCreature, nonCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, idol));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(expensiveCreature, nonCreature, idol.getCard(), cheapCreature);
    }

    @Test
    @DisplayName("Activated ability grants one later free creature cast from the Idol's exile")
    void activatedAbilityGrantsOneFreeCreatureCast() {
        Permanent idol = addReadyIdol();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        gd.addToExile(player1.getId(), firstCreature, idol.getId());
        gd.addToExile(player1.getId(), secondCreature, idol.getId());
        addActivationMana();

        activateIdol(idol);
        harness.castFromExile(player1, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstCreature.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, secondCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability only permits creature cards and expires at end of turn")
    void activatedAbilityFiltersCardsAndExpires() {
        Permanent idol = addReadyIdol();
        Card creature = new GrizzlyBears();
        Card nonCreature = new Shock();
        gd.addToExile(player1.getId(), creature, idol.getId());
        gd.addToExile(player1.getId(), nonCreature, idol.getId());
        addActivationMana();

        activateIdol(idol);

        assertThatThrownBy(() -> harness.castFromExile(player1, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A card that returns to exile is no longer linked to the Idol")
    void cardThatReturnsToExileIsNotReturnedWithIdol() {
        Permanent idol = addReadyIdol();
        Card creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, idol.getId());

        gd.removeFromExile(creature.getId());
        gd.addToExile(player1.getId(), creature);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, idol));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addReadyIdol() {
        Permanent idol = new Permanent(new IdolOfEndurance());
        idol.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(idol);
        return idol;
    }

    private void addActivationMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateIdol(Permanent idol) {
        int idolIndex = gd.playerBattlefields.get(player1.getId()).indexOf(idol);
        harness.activateAbility(player1, idolIndex, null, null);
        harness.passBothPriorities();
    }
}
