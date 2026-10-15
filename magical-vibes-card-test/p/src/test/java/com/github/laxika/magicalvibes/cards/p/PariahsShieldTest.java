package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RainOfEmbers;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PariahsShield.class, RainOfEmbers.class, Watchwolf.class, ViashinoFangtail.class, Phytohydra.class})
class PariahsShieldTest extends BaseCardTest {

    @Test
    void equipAbilityAttachesShieldToTargetCreature() {
        Permanent shield = addShieldReady();
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipAbilityCannotTargetAnOpponentCreature() {
        Permanent shield = addShieldReady();
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.getAttachedTo()).isNull();
    }

    @Test
    void damageToControllerIsRedirectedToEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(creature.getId());
        addCreatureReady(player1, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void unattachedShieldDoesNotRedirectDamage() {
        addShieldReady(player2);
        addCreatureReady(player1, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void noncombatDamageToControllerIsRedirectedToEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(creature.getId());

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void reequippingRedirectsDamageOnlyToTheNewCreature() {
        Permanent shield = addShieldReady();
        Permanent first = addCreatureReady(player1, new Watchwolf());
        Permanent second = addCreatureReady(player1, new Watchwolf());
        shield.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(second.getId());
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void simultaneousCombatDamageIsAllRedirectedEvenWhenItIsLethal() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        addShieldReady(player2).setAttachedTo(creature.getId());
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player1, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        harness.assertOnBattlefield(player2, "Pariah's Shield");
    }

    @Test
    void shieldProtectsItsControllerWhenEquippedCreatureHasAnotherController() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        addShieldReady(player1).setAttachedTo(creature.getId());

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void redirectedDamageCanBeReplacedByPhytohydrasCounters() {
        Permanent creature = addCreatureReady(player2, new Phytohydra());
        addShieldReady(player2).setAttachedTo(creature.getId());

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Phytohydra");
    }

    @Test
    void controllerChoosesWhichShieldRedirectsAnIncomingDamageEvent() {
        Permanent first = addCreatureReady(player2, new Watchwolf());
        Permanent second = addCreatureReady(player2, new Watchwolf());
        addShieldReady(player2).setAttachedTo(first.getId());
        addShieldReady(player2).setAttachedTo(second.getId());
        addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, second.getId());

        harness.assertLife(player2, 20);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addShieldReady() {
        return addShieldReady(player1);
    }

    private Permanent addShieldReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PariahsShield());
    }
}
