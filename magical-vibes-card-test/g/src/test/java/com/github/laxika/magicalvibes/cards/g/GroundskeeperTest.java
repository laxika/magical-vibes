package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Groundskeeper.class, Forest.class, YavimayaCoast.class})
class GroundskeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        var groundskeeper = harness.addToBattlefieldAndReturn(player1, new Groundskeeper());
        groundskeeper.tap();
        groundskeeper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card firstForest = new Forest();
        Card secondForest = new Forest();
        harness.setGraveyard(player1, List.of(firstForest, secondForest));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(firstForest.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(secondForest.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstForest, secondForest);
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(groundskeeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after Groundskeeper leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate with only one green mana")
    void cannotActivateWithoutGenericMana() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("{1}{G}: Return target basic land card from graveyard to hand")
    void returnBasicLandFromGraveyard() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Activation pays one generic and one green mana")
    void activationPaysManaCost() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot target a nonbasic land")
    void cannotReturnNonbasicLand() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card yavimayaCoast = new YavimayaCoast();
        harness.setGraveyard(player1, List.of(yavimayaCoast));

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(yavimayaCoast.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland card")
    void cannotReturnNonlandCard() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card creature = new Groundskeeper();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a basic land in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the targeted basic land leaves the graveyard before resolution")
    void doesNothingIfTargetLeavesGraveyard() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
    }
}
