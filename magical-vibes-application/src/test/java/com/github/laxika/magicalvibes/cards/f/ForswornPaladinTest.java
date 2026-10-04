package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForswornPaladin.class})
class ForswornPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability costs 1 life and creates a Treasure")
    void createsTreasureAndPaysLife() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability pumps a target creature without granting deathtouch from ordinary mana")
    void ordinaryManaDoesNotGrantDeathtouch() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, paladin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The second ability grants deathtouch when Treasure mana pays its colored cost")
    void treasureManaGrantsDeathtouch() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, paladin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void treasureManaForGenericCostCanGrantDeathtouchToOpposingCreatureUntilEndOfTurn() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        Permanent target = addCreatureReady(player2, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void treasurePaymentIsRememberedSeparatelyForEachStackedActivation() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        Permanent ordinaryTarget = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, paladin.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, ordinaryTarget.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ordinaryTarget)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ordinaryTarget, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void lifeAndTapArePaidBeforeTreasureAbilityResolves() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(paladin.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void summoningSicknessPreventsTreasureAbilityButAllowsPumpAbility() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new ForswornPaladin());
        paladin.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(paladin.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.activateAbility(player1, 0, 1, null, paladin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isFalse();
    }
}
