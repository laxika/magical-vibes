package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
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

@CardUsed({BoundInGold.class, FountainOfYouth.class, GrizzlyBears.class,
        LlanowarElves.class, SmugglersCopter.class})
class BoundInGoldTest extends BaseCardTest {

    @Test
    @DisplayName("Bound in Gold can enchant a permanent and stops its non-mana abilities")
    void enchantsPermanentAndStopsNonManaAbilities() {
        Permanent fountain = addPermanent(player1, new FountainOfYouth());
        castBoundInGold(player2, fountain);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature can't attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castBoundInGold(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature can't block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castBoundInGold(player1, blocker);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Bound in Gold does not stop mana abilities")
    void manaAbilityRemainsUsable() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        castBoundInGold(player2, elves);

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
        castBoundInGold(player2, enchantedCreature);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, copter)).isTrue();
    }

    @Test
    @DisplayName("An enchanted Vehicle cannot activate its own crew ability")
    void enchantedVehicleCannotActivateCrew() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent copter = addPermanent(player1, new SmugglersCopter());
        castBoundInGold(player2, copter);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, copter)).isFalse();
    }

    @Test
    @DisplayName("Non-mana abilities become usable again when Bound in Gold leaves")
    void abilityBecomesUsableAfterAuraLeaves() {
        Permanent fountain = addPermanent(player1, new FountainOfYouth());
        castBoundInGold(player2, fountain);
        Permanent aura = findPermanent(player2, "Bound in Gold");
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castBoundInGold(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.setHand(controller, List.of(new BoundInGold()));
        harness.addMana(controller, ManaColor.WHITE, 3);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }
}
