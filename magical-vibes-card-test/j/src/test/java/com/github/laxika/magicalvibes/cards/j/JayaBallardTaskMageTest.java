package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.d.DrudgeReavers;
import com.github.laxika.magicalvibes.cards.s.SageOfEpityr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JayaBallardTaskMage.class, SageOfEpityr.class, BenalishCavalry.class, DrudgeReavers.class})
class JayaBallardTaskMageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target blue permanent after discarding a card")
    void destroysTargetBluePermanent() {
        Permanent jaya = addCreatureReady(player1, new JayaBallardTaskMage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SageOfEpityr());
        Card discarded = new BenalishCavalry();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sage of Epityr");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(jaya.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-blue permanent")
    void cannotTargetNonBluePermanent() {
        addCreatureReady(player1, new JayaBallardTaskMage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue permanent");
    }

    @Test
    @DisplayName("Deals 3 damage to any target and prevents regeneration of a damaged creature")
    void dealsDamageAndPreventsRegeneration() {
        addCreatureReady(player1, new JayaBallardTaskMage());
        Permanent reavers = addCreatureReady(player2, new DrudgeReavers());
        reavers.setRegenerationShield(1);
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, reavers.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drudge Reavers");
        assertThat(reavers.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 3 damage to a player")
    void dealsDamageToPlayer() {
        addCreatureReady(player1, new JayaBallardTaskMage());
        Card discarded = new BenalishCavalry();
        harness.setHand(player1, List.of(discarded));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Deals 6 damage to each creature and each player")
    void dealsDamageToEachCreatureAndPlayer() {
        Permanent jaya = addCreatureReady(player1, new JayaBallardTaskMage());
        harness.addToBattlefield(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        harness.assertInGraveyard(player1, "Jaya Ballard, Task Mage");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
        assertThat(jaya.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The six-damage ability allows regeneration")
    void massDamageAllowsRegeneration() {
        addCreatureReady(player1, new JayaBallardTaskMage());
        Permanent reavers = addCreatureReady(player2, new DrudgeReavers());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drudge Reavers");
        harness.assertNotInGraveyard(player2, "Drudge Reavers");
        assertThat(reavers.getRegenerationShield()).isZero();
        assertThat(reavers.getMarkedDamage()).isZero();
        assertThat(reavers.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Jaya Ballard, Task Mage");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithEmptyHand() {
        addCreatureReady(player1, new JayaBallardTaskMage());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate a tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new JayaBallardTaskMage());
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Benalish Cavalry");
    }

    @Test
    @DisplayName("The second ability can target Jaya herself")
    void canDealDamageToHerself() {
        Permanent jaya = addCreatureReady(player1, new JayaBallardTaskMage());
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, jaya.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jaya Ballard, Task Mage");
        harness.assertInGraveyard(player1, "Benalish Cavalry");
        harness.assertNotOnBattlefield(player1, "Jaya Ballard, Task Mage");
    }
}
