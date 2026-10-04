package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HammerOfPurphoros.class, Forest.class, GrizzlyBears.class, MarchOfTheMachines.class})
class HammerOfPurphorosTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures you control, but not to an opponent's creatures")
    void grantsHasteToOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new HammerOfPurphoros());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifices a land to create a hasty 3/3 colorless Golem enchantment artifact creature token")
    void sacrificesLandAndCreatesGolemToken() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new HammerOfPurphoros());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(hammer.isTapped()).isTrue();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(3);
        assertThat(golem.getCard().getColors()).isEmpty();
        assertThat(golem.getCard().getSubtypes()).contains(CardSubtype.GOLEM);
        assertThat(golem.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(golem.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(golem.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new HammerOfPurphoros());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animatedHammerGrantsItselfHasteAndCanActivateImmediately() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new HammerOfPurphoros());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThat(gqs.isCreature(gd, hammer)).isTrue();
        assertThat(gqs.hasKeyword(gd, hammer, Keyword.HASTE)).isTrue();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Golem");
        assertThat(hammer.isTapped()).isTrue();
    }

    @Test
    void landIsSacrificedAsCostAndAbilityResolvesAfterHammerLeaves() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new HammerOfPurphoros());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Golem");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hammer));
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isFalse();
        assertThat(golem.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Golem")).isEqualTo(1);
    }

    @Test
    void cannotActivateTappedHammer() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new HammerOfPurphoros());
        hammer.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void opponentsLandCannotPaySacrificeCost() {
        harness.addToBattlefield(player1, new HammerOfPurphoros());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void activationRequiresRedMana() {
        harness.addToBattlefield(player1, new HammerOfPurphoros());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }
}
