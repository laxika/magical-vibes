package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cindervines;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrygonPredator.class, FountainOfYouth.class, Cindervines.class, GrizzlyBears.class})
class TrygonPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may destroy an artifact or enchantment controlled by the damaged player")
    void acceptingTriggerDestroysArtifactOrEnchantment() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enemyEnchantment = harness.addToBattlefieldAndReturn(player2, new Cindervines());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(enemyArtifact.getId(), enemyEnchantment.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(enemyEnchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Cindervines");
        harness.assertInGraveyard(player2, "Cindervines");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Declining the combat-damage trigger destroys nothing")
    void decliningTriggerDestroysNothing() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        harness.addToBattlefield(player2, new FountainOfYouth());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The trigger does not occur when the damaged player controls no artifact or enchantment")
    void noMatchingPermanentMeansNoTrigger() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
