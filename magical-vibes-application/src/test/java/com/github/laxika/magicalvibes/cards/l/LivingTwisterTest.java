package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingTwister.class, Forest.class, GrizzlyBears.class, Mountain.class})
class LivingTwisterTest extends BaseCardTest {

    @Test
    @DisplayName("The damage ability discards a land and deals 2 damage to a player")
    void damageAbilityDiscardsLandAndDamagesPlayer() {
        addLivingTwister();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The damage ability can deal damage to a creature")
    void damageAbilityDamagesCreature() {
        addLivingTwister();
        harness.setHand(player1, List.of(new Mountain()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The return ability chooses only a tapped land the controller controls")
    void returnAbilityChoosesTappedControlledLand() {
        addLivingTwister();
        Permanent tappedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent untappedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        UUID tappedForestId = tappedForest.getId();
        UUID untappedForestId = untappedForest.getId();
        harness.tapPermanent(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(tappedForestId);
        harness.handlePermanentChosen(player1, tappedForestId);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(untappedForestId))
                .noneMatch(permanent -> permanent.getId().equals(tappedForestId));
    }

    @Test
    @DisplayName("The return ability does nothing when no tapped land is controlled")
    void returnAbilityDoesNothingWithoutTappedLand() {
        addLivingTwister();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        UUID forestId = forest.getId();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(forestId));
        harness.assertNotInHand(player1, "Forest");
    }

    private Permanent addLivingTwister() {
        Permanent permanent = new Permanent(new LivingTwister());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
