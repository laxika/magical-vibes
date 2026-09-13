package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Masticore.class, BraidwoodCup.class})
class MasticoreTest extends BaseCardTest {

    @Test
    void upkeepCanBePaidByDiscardingAnyCard() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of(new BraidwoodCup()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Braidwood Cup");
    }

    @Test
    void upkeepSacrificesMasticoreWhenDiscardIsDeclined() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of(new BraidwoodCup()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
    }

    @Test
    void upkeepSacrificesMasticoreWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void damageAbilityDealsOneDamageToTargetCreature() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID masticoreId = harness.getPermanentId(player2, "Masticore");

        harness.activateAbility(player1, 0, 0, null, masticoreId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Masticore");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void damageAbilityRejectsPlayerTarget() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    @Test
    void damageAbilityRejectsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new BraidwoodCup());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID cupId = harness.getPermanentId(player2, "Braidwood Cup");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, cupId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void abilitiesRequireTwoColorlessMana() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID masticoreId = harness.getPermanentId(player2, "Masticore");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, masticoreId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void regenerationAbilityGrantsARegenerationShield() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesMasticoreFromLethalDamage() {
        Permanent masticore = addCreatureReady(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, 0, null, masticore.getId());
        }
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Masticore");
        Permanent survivor = findPermanent(player1, "Masticore");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
        assertThat(survivor.getMarkedDamage()).isZero();
    }

    @Test
    void upkeepTriggerOnlyFiresDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player2);

        harness.assertOnBattlefield(player1, "Masticore");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
