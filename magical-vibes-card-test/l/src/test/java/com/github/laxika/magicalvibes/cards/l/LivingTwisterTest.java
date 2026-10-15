package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingTwister.class, Forest.class, GrizzlyBears.class, Mountain.class, JayaVeneratedFiremage.class})
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

    @Test
    @DisplayName("Only land cards can be discarded, and the discard is paid before resolution")
    void damageAbilityRequiresLandDiscardBeforeResolution() {
        addLivingTwister();
        harness.setHand(player1, List.of(new LivingTwister(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Living Twister");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The damage ability works while Living Twister is tapped and summoning sick")
    void damageAbilityDoesNotRequireTappingOrHaste() {
        Permanent twister = harness.addToBattlefieldAndReturn(player1, new LivingTwister());
        twister.setSummoningSick(true);
        twister.tap();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The return ability cannot choose an opponent's tapped land or a tapped nonland")
    void returnAbilityExcludesOpposingLandsAndNonlands() {
        Permanent twister = harness.addToBattlefieldAndReturn(player1, new LivingTwister());
        twister.tap();
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownForest.tap();
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        opposingForest.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownForest.getId());
        harness.handlePermanentChosen(player1, ownForest.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Living Twister");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A land tapped after activation can be chosen when the return ability resolves")
    void returnAbilityChoosesLandAtResolution() {
        addLivingTwister();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.tapPermanent(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The damage ability can target a planeswalker")
    void damageAbilityDamagesPlaneswalker() {
        addLivingTwister();
        Permanent jaya = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, jaya.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("The damage ability cannot be activated without a land card in hand")
    void damageAbilityCannotDiscardNonland() {
        addLivingTwister();
        harness.setHand(player1, List.of(new LivingTwister()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Living Twister");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land controlled by Living Twister's controller returns to its owner")
    void returnAbilityReturnsBorrowedLandToOwner() {
        addLivingTwister();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        gd.stolenCreatures.put(forest.getId(), player2.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private void addLivingTwister() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new LivingTwister());
        permanent.setSummoningSick(false);
    }
}
