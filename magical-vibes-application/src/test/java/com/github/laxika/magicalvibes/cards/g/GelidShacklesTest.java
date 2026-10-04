package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.r.RimeboundDead;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GelidShackles.class, RimeboundDead.class, BorealDruid.class})
class GelidShacklesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent enchanted = readyCreature(player1);
        attachedShackles(player2, enchanted);
        Permanent attacker = readyCreature(player2);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature's activated abilities cannot be activated")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent enchanted = readyCreature(player1);
        attachedShackles(player2, enchanted);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Snow ability gives the enchanted creature defender until end of turn")
    void snowAbilityGivesDefenderUntilEndOfTurn() {
        Permanent enchanted = readyCreature(player1);
        attachedShackles(player1, enchanted);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Defender from the snow ability wears off at end of turn")
    void defenderWearsOffAtEndOfTurn() {
        Permanent enchanted = readyCreature(player1);
        attachedShackles(player1, enchanted);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DEFENDER)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Casting the Aura attaches it to an opponent's creature and locks its ability")
    void castingAuraAttachesToOpponentsCreature() {
        Permanent creature = readyCreature(player2);
        harness.setHand(player1, List.of(new GelidShackles()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gelid Shackles").getAttachedTo()).isEqualTo(creature.getId());
        gd.playerManaPools.get(player2.getId()).addSnowMana(ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature's mana abilities cannot be activated either")
    void enchantedCreatureCannotActivateManaAbilities() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        attachedShackles(player2, creature);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Enchanted creature can attack before the snow ability is used")
    void enchantedCreatureCanAttackWithoutDefender() {
        Permanent creature = readyCreature(player1);
        attachedShackles(player2, creature);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Ordinary mana cannot pay the snow activation cost")
    void ordinaryManaCannotPaySnowCost() {
        Permanent creature = readyCreature(player1);
        attachedShackles(player1, creature);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Aura controller can give an opponent's creature defender using colored snow mana")
    void auraControllerCanGiveOpponentsCreatureDefender() {
        Permanent creature = readyCreature(player2);
        attachedShackles(player1, creature);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura restores activation but does not remove already granted defender")
    void removingAuraDoesNotRemoveGrantedDefender() {
        Permanent creature = readyCreature(player1);
        Permanent shackles = attachedShackles(player1, creature);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(shackles);
        gd.playerGraveyards.get(player1.getId()).add(shackles.getCard());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent readyCreature(Player player) {
        return addCreatureReady(player, new RimeboundDead());
    }

    private Permanent attachedShackles(Player controller, Permanent creature) {
        Permanent shackles = harness.addToBattlefieldAndReturn(controller, new GelidShackles());
        shackles.setAttachedTo(creature.getId());
        return shackles;
    }
}
