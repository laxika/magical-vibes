package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinflameTyrant.class, Blaze.class, GrizzlyBears.class, SerraAngel.class,
        WitnessProtection.class, SamiteHealer.class})
class TwinflameTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles damage from your spell to an opponent")
    void doublesSpellDamageToOpponent() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Doubles damage from your spell to an opponent's permanent")
    void doublesSpellDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveSorcery(player1, 0, 2, serraId);

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double damage to you or your permanents")
    void doesNotDoubleDamageToControllerOrTheirPermanents() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player1, 20);

        UUID serraId = harness.getPermanentId(player1, "Serra Angel");
        harness.castAndResolveSorcery(player1, 0, 2, serraId);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not double damage from a source controlled by an opponent")
    void doesNotDoubleOpponentsSourceDamage() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Doubles your combat damage to an opponent")
    void doublesCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TwinflameTyrant());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotDoubleSpellDamageToController() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void multipleTyrantsMultiplyDamageIndependently() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 8);
    }

    @Test
    void doublesItsOwnCombatDamage() {
        addCreatureReady(player1, new TwinflameTyrant());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void doublesCombatDamageToBlockerButNotOpponentsDamage() {
        Permanent attacker = addCreatureReady(player1, new TwinflameTyrant());
        harness.addToBattlefield(player2, new SerraAngel());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Twinflame Tyrant");
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void stopsDoublingAfterTyrantIsDestroyed() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 5,
                harness.getPermanentId(player1, "Twinflame Tyrant"));
        harness.assertInGraveyard(player1, "Twinflame Tyrant");
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void stopsDoublingWhenWitnessProtectionRemovesItsAbilities() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new TwinflameTyrant());
        harness.setHand(player1, List.of(new WitnessProtection(), new Blaze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castEnchantment(player1, 0, tyrant.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void damagedPlayerChoosesOrderOfPreventionAndDoubling() {
        harness.addToBattlefield(player1, new TwinflameTyrant());
        addCreatureReady(player2, new SamiteHealer());
        harness.setLife(player2, 20);
        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
    }
}
