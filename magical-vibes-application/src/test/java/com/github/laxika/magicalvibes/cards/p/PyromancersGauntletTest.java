package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.f.FlamesOfTheFirebrand;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.s.StaffOfNin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyromancersGauntlet.class, Shock.class, SerraAngel.class, ChandraNalaar.class,
        ConsumeSpirit.class, StaffOfNin.class, FlamesOfTheFirebrand.class, SongOfTheDryads.class})
class PyromancersGauntletTest extends BaseCardTest {

    @Test
    @DisplayName("Red instant deals plus 2 damage to a player")
    void redInstantPlusTwoToPlayer() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Red instant deals plus 2 damage to a creature")
    void redInstantPlusTwoToCreature() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Red planeswalker ability deals plus 2 damage")
    void redPlaneswalkerPlusTwo() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        addReadyChandra(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not boost nonred spells")
    void doesNotBoostNonRedSpells() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not boost non-planeswalker activated ability damage")
    void doesNotBoostArtifactAbilityDamage() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfNin());
        staff.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not boost opponent's red spells")
    void doesNotBoostOpponentsRedSpells() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Gauntlets stack additively")
    void twoGauntletsStack() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void redSorceryAddsTwoToEachDamageRecipient() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(angel.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    void boostsDamageToItsControllersPlayer() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    void boostsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        Permanent chandra = addReadyChandra(player2);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void zeroDamageFromRedPlaneswalkerIsNotIncreased() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        addReadyChandra(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.activateAbility(player1, 1, 1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    void doesNotBoostOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        addReadyChandra(player2);

        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void gauntletThatLostItsPrintedAbilityDoesNotBoostDamage() {
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new PyromancersGauntlet());
        harness.setHand(player1, List.of(new SongOfTheDryads(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, gauntlet.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void queuedAbilityFromPlaneswalkerNowAColorlessForestIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        Permanent chandra = addReadyChandra(player1);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SongOfTheDryads());
        aura.setAttachedTo(chandra.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
