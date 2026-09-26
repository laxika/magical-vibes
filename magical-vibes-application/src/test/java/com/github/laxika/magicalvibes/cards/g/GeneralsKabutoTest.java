package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HanabiBlast;
import com.github.laxika.magicalvibes.cards.k.KamiOfAncientLaw;
import com.github.laxika.magicalvibes.cards.k.KamiOfThePaintedRoad;
import com.github.laxika.magicalvibes.cards.y.YamabushisStorm;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralsKabuto.class, KamiOfAncientLaw.class, KamiOfThePaintedRoad.class,
        HanabiBlast.class, YamabushisStorm.class})
class GeneralsKabutoTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        addKabutoAttached(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature can't be targeted by an opponent's spell")
    void equippedCreatureCannotBeTargeted() {
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        addKabutoAttached(player1, creature);
        harness.setHand(player2, List.of(new HanabiBlast()));
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature can't be targeted by its controller's spell")
    void equippedCreatureCannotBeTargetedByItsController() {
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        addKabutoAttached(player1, creature);
        harness.setHand(player1, List.of(new HanabiBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature loses shroud when the Kabuto is removed")
    void creatureLosesShroudWhenKabutoRemoved() {
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        Permanent kabuto = addKabutoAttached(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(kabuto);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Combat damage dealt to the equipped creature is prevented")
    void combatDamageToEquippedCreatureIsPrevented() {
        Permanent blocker = addCreatureReady(player1, new KamiOfAncientLaw());
        addKabutoAttached(player1, blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KamiOfThePaintedRoad());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // Kami of the Painted Road's 3 damage would kill a 2/2, but it is prevented.
        harness.assertOnBattlefield(player1, "Kami of Ancient Law");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The equipped creature still deals its own combat damage")
    void equippedCreatureStillDealsCombatDamage() {
        Permanent blocker = addCreatureReady(player1, new KamiOfAncientLaw());
        addKabutoAttached(player1, blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KamiOfThePaintedRoad());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to the equipped creature is not prevented")
    void noncombatDamageIsNotPrevented() {
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        addKabutoAttached(player1, creature);
        // Yamabushi's Storm doesn't target, so shroud doesn't stop it, and its damage isn't combat damage.
        harness.setHand(player1, List.of(new YamabushisStorm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Kami of Ancient Law");
    }

    @Test
    @DisplayName("Combat damage is no longer prevented once the Kabuto is unattached")
    void preventionStopsWhenUnattached() {
        Permanent blocker = addCreatureReady(player1, new KamiOfAncientLaw());
        Permanent kabuto = addKabutoAttached(player1, blocker);
        kabuto.setAttachedTo(null);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KamiOfThePaintedRoad());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Kami of Ancient Law");
    }

    @Test
    @DisplayName("Resolving equip attaches the Kabuto to target creature")
    void resolvingEquipAttaches() {
        Permanent kabuto = addKabutoReady(player1);
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kabuto.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip costs two mana")
    void equipCostsTwoMana() {
        Permanent kabuto = addKabutoReady(player1);
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(kabuto.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can target only a creature its controller controls")
    void equipCannotTargetOpponentCreature() {
        Permanent kabuto = addKabutoReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new KamiOfAncientLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kabuto.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can be activated only during its controller's main phase")
    void equipCannotBeActivatedOnOpponentTurn() {
        Permanent kabuto = addKabutoReady(player1);
        Permanent creature = addCreatureReady(player1, new KamiOfAncientLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(kabuto.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Re-equipping transfers shroud to the new creature")
    void reequippingTransfersShroud() {
        Permanent firstCreature = addCreatureReady(player1, new KamiOfAncientLaw());
        Permanent kabuto = addKabutoAttached(player1, firstCreature);
        Permanent secondCreature = addCreatureReady(player1, new KamiOfAncientLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(kabuto.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.SHROUD)).isTrue();
    }

    private Permanent addKabutoReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GeneralsKabuto());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addKabutoAttached(Player player, Permanent creature) {
        Permanent perm = addKabutoReady(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }
}
