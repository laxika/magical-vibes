package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreyKeeper.class, MaskwoodNexus.class})
class DreyKeeperTest extends BaseCardTest {

    @Test
    void entersAndCreatesTwoSquirrels() {
        harness.castFromHand(player1, new DreyKeeper(), "{3}{B}{G}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
        assertThat(findPermanents(player2, "Squirrel")).isEmpty();
        for (Permanent squirrel : findPermanents(player1, "Squirrel")) {
            assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
        }
    }

    @Test
    void abilityBoostsOnlyYourSquirrels() {
        enterKeeper(player1);
        Permanent ownSquirrel = findPermanent(player1, "Squirrel");
        Permanent ownNonSquirrel = addCreatureReady(player1, new DreyKeeper());
        enterKeeper(player2);
        Permanent opponentSquirrel = findPermanent(player2, "Squirrel");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownSquirrel, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownNonSquirrel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownNonSquirrel, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentSquirrel, Keyword.MENACE)).isFalse();
    }

    @Test
    void abilityExpiresAtEndOfTurn() {
        enterKeeper(player1);
        Permanent squirrel = findPermanent(player1, "Squirrel");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, squirrel, Keyword.MENACE)).isFalse();
    }

    @Test
    void abilityGrantsMenaceToItsSourceWhenItIsASquirrel() {
        Permanent keeper = enterKeeper(player1);
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, keeper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, keeper, Keyword.MENACE)).isTrue();
    }

    @Test
    void repeatedActivationsStackAndDoNotAffectLaterSquirrels() {
        enterKeeper(player1);
        Permanent squirrel = findPermanent(player1, "Squirrel");
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, squirrel, Keyword.MENACE)).isTrue();

        enterKeeper(player1);
        var squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(4);
        for (Permanent laterSquirrel : squirrels.subList(2, 4)) {
            assertThat(gqs.getEffectivePower(gd, laterSquirrel)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, laterSquirrel, Keyword.MENACE)).isFalse();
        }
    }

    private Permanent enterKeeper(Player player) {
        Permanent permanent = harness.enterBattlefieldAndReturn(player, new DreyKeeper());
        resolveAllTriggers();
        return permanent;
    }
}
