package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BragoKingEternal.class, GrizzlyBears.class, Forest.class, LayClaim.class})
class BragoKingEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage flickers any number of targeted nonland permanents")
    void flickersSelectedNonlandPermanents() {
        Permanent brago = addAttackingBrago();
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, firstBear.getId());
        harness.handlePermanentChosen(player1, secondBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2)
                .extracting(Permanent::getId)
                .doesNotContain(firstBear.getId(), secondBear.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brago);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger only offers controlled nonland permanents")
    void onlyOffersControlledNonlandPermanents() {
        Permanent brago = addAttackingBrago();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombatToTargetChoice();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(brago.getId(), ownBear.getId())
                .doesNotContain(ownForest.getId(), opponentBear.getId());
    }

    @Test
    @DisplayName("Flickered permanents return under their owners' control")
    void returnsUnderOwnersControl() {
        Permanent stolenBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, stolenBear.getId());
        harness.passBothPriorities();

        Permanent brago = addAttackingBrago();
        Permanent controlledBear = gqs.findPermanentById(
                gd, harness.getPermanentId(player1, "Grizzly Bears"));

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, controlledBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brago);
    }

    private Permanent addAttackingBrago() {
        Permanent brago = harness.addToBattlefieldAndReturn(player1, new BragoKingEternal());
        brago.setSummoningSick(false);
        brago.setAttacking(true);
        return brago;
    }

    private void resolveCombatToTargetChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
