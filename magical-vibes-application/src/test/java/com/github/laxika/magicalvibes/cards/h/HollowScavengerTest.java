package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BakeryRaid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowScavenger.class, BakeryRaid.class})
class HollowScavengerTest extends BaseCardTest {

    @Test
    void adventureCreatesFoodAndExilesTheCard() {
        HollowScavenger card = new HollowScavenger();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAndSacrificeFoodForTemporaryBoost() {
        HollowScavenger card = castAdventure();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Hollow Scavenger");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(4);
    }

    @Test
    void boostAbilityCanBeActivatedOnlyOnceEachTurn() {
        HollowScavenger card = castAdventure();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Hollow Scavenger");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void foodIsSacrificedAsACostBeforeTheBoostResolves() {
        castAdventure();
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new HollowScavenger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(4);
    }

    @Test
    void cannotSacrificeAnOpponentsFood() {
        HollowScavenger adventure = new HollowScavenger();
        harness.setHand(player2, List.of(adventure));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAdventure(player2, 0, List.of());
        harness.passBothPriorities();
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new HollowScavenger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a Food");
        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureFoodCanBeSacrificedForThreeLife() {
        castAdventure();
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, food), null, null);
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    void boostExpiresAndAbilityCanBeActivatedOnTheOpponentsTurn() {
        HollowScavenger adventure = castAdventure();
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new HollowScavenger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(2);
        assertThat(gd.findExiledCard(adventure.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(adventure.getId())).isEqualTo(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        castAdventure();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(4);
    }

    private HollowScavenger castAdventure() {
        HollowScavenger card = new HollowScavenger();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        return card;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
