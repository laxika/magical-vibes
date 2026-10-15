package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntercessorsArrest.class, FountainOfYouth.class, GrizzlyBears.class,
        LlanowarElves.class, SmugglersCopter.class, TrainedArynx.class})
class IntercessorsArrestTest extends BaseCardTest {

    @Test
    @DisplayName("Intercessor's Arrest can enchant a permanent and stops its non-mana abilities")
    void enchantsPermanentAndStopsNonManaAbilities() {
        Permanent fountain = addPermanent(player1, new FountainOfYouth());
        castIntercessorsArrest(player2, fountain);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature can't attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castIntercessorsArrest(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature can't block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castIntercessorsArrest(player1, blocker);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature's mana ability remains usable")
    void manaAbilityRemainsUsable() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        castIntercessorsArrest(player2, elves);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can't crew a Vehicle")
    void enchantedCreatureCannotCrewVehicle() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent copter = addPermanent(player1, new SmugglersCopter());
        castIntercessorsArrest(player2, enchantedCreature);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, copter)).isTrue();
    }

    @Test
    @DisplayName("An enchanted Vehicle cannot activate its own crew ability")
    void enchantedVehicleCannotActivateCrew() {
        Permanent copter = addPermanent(player1, new SmugglersCopter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castIntercessorsArrest(player2, copter);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, copter)).isFalse();
    }

    @Test
    @DisplayName("Crew cannot be paid when the only available creature is enchanted")
    void enchantedCreatureCannotBeTheOnlyCrewPayment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addPermanent(player1, new SmugglersCopter());
        castIntercessorsArrest(player2, creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An enchanted creature can still saddle another permanent")
    void enchantedCreatureCanSaddleMount() {
        Permanent mount = addCreatureReady(player1, new TrainedArynx());
        Permanent rider = addCreatureReady(player1, new TrainedArynx());
        castIntercessorsArrest(player1, rider);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rider.isTapped()).isTrue();
        assertThat(mount.isSaddled()).isTrue();
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castIntercessorsArrest(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.setHand(controller, List.of(new IntercessorsArrest()));
        harness.addMana(controller, ManaColor.WHITE, 3);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }
}
