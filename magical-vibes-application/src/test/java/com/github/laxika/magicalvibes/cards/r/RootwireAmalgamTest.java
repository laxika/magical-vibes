package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwireAmalgam.class, GiantGrowth.class})
class RootwireAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Rootwire Amalgam creates a hasty artifact Golem three times its power")
    void createsGolemWithTriplePowerAndHaste() {
        harness.addToBattlefieldAndReturn(player1, new RootwireAmalgam());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rootwire Amalgam");
        harness.assertInGraveyard(player1, "Rootwire Amalgam");
        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().getPower()).isEqualTo(15);
        assertThat(golem.getCard().getToughness()).isEqualTo(15);
        assertThat(golem.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Prototype power is used when creating the Golem")
    void prototypeCreatesGolemWithSixPower() {
        harness.setHand(player1, List.of(new RootwireAmalgam()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().getPower()).isEqualTo(6);
        assertThat(golem.getCard().getToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The ability can only be activated as a sorcery")
    void abilityIsSorcerySpeedOnly() {
        harness.addToBattlefieldAndReturn(player1, new RootwireAmalgam());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The full-cost spell creates a Golem using its non-prototype power")
    void fullCostSpellCreatesFifteenPowerGolem() {
        harness.setHand(player1, List.of(new RootwireAmalgam()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Rootwire Amalgam");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().getPower()).isEqualTo(15);
        assertThat(golem.getCard().getToughness()).isEqualTo(15);
    }

    @Test
    @DisplayName("The Golem uses modified power immediately before sacrifice")
    void usesLastKnownModifiedPower() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new RootwireAmalgam());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, amalgam.getId());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().getPower()).isEqualTo(24);
        assertThat(golem.getCard().getToughness()).isEqualTo(24);
    }

    @Test
    @DisplayName("The ability cannot be activated while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new RootwireAmalgam());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, amalgam.getId());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Rootwire Amalgam");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Insufficient mana does not sacrifice Rootwire Amalgam")
    void cannotActivateWithoutPayingFullManaCost() {
        harness.addToBattlefieldAndReturn(player1, new RootwireAmalgam());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Rootwire Amalgam");
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
