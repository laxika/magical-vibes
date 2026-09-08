package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent golem = findGolem(player1);
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

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent golem = findGolem(player1);
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

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private Permanent findGolem(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Golem"))
                .findFirst()
                .orElseThrow();
    }
}
