package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.d.DuskriderFalcon;
import com.github.laxika.magicalvibes.cards.h.HeavyBallista;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        KithkinArmor.class,
        BenalishKnight.class,
        DuskriderFalcon.class,
        HeavyBallista.class,
        RedwoodTreefolk.class,
        Thunderbolt.class
})
class KithkinArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature with power 3")
    void cannotBeBlockedByPowerThree() {
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());
        attacker.setAttacking(true);
        attachArmor(attacker);

        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a creature with power 2")
    void canBeBlockedByPowerTwo() {
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());
        attacker.setAttacking(true);
        attachArmor(attacker);

        Permanent blocker = addCreatureReady(player2, new BenalishKnight());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura prevents the chosen source's next damage to the enchanted creature")
    void preventsNextDamageFromChosenSource() {
        Permanent enchanted = addCreatureReady(player1, new RedwoodTreefolk());
        enchanted.setAttacking(true);
        Permanent armor = attachArmor(enchanted);
        Permanent ballista = addCreatureReady(player2, new HeavyBallista());

        chooseDamageSource(armor, ballista.getId());

        harness.assertInGraveyard(player1, "Kithkin Armor");

        harness.activateAbility(player2, indexOf(player2, ballista), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The shield only covers the enchanted creature, not other permanents")
    void shieldDoesNotCoverOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new RedwoodTreefolk());
        enchanted.setAttacking(true);
        Permanent armor = attachArmor(enchanted);
        Permanent other = addCreatureReady(player1, new RedwoodTreefolk());
        other.setAttacking(true);
        Permanent ballista = addCreatureReady(player2, new HeavyBallista());

        chooseDamageSource(armor, ballista.getId());

        harness.activateAbility(player2, indexOf(player2, ballista), null, other.getId());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(2);

        ballista.untap();
        harness.activateAbility(player2, indexOf(player2, ballista), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
        assertThat(other.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the next damage event from the chosen source is prevented")
    void onlyTheNextDamageEventIsPrevented() {
        Permanent enchanted = addCreatureReady(player1, new RedwoodTreefolk());
        enchanted.setAttacking(true);
        Permanent armor = attachArmor(enchanted);
        Permanent ballista = addCreatureReady(player2, new HeavyBallista());

        chooseDamageSource(armor, ballista.getId());

        harness.activateAbility(player2, indexOf(player2, ballista), null, enchanted.getId());
        harness.passBothPriorities();
        assertThat(enchanted.getMarkedDamage()).isZero();

        ballista.untap();
        harness.activateAbility(player2, indexOf(player2, ballista), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void otherSourceIsNotPrevented() {
        Permanent enchanted = addCreatureReady(player1, new RedwoodTreefolk());
        enchanted.setAttacking(true);
        Permanent armor = attachArmor(enchanted);
        Permanent chosen = addCreatureReady(player2, new HeavyBallista());
        Permanent other = addCreatureReady(player2, new HeavyBallista());

        chooseDamageSource(armor, chosen.getId());

        harness.activateAbility(player2, indexOf(player2, other), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);

        chosen.untap();
        harness.activateAbility(player2, indexOf(player2, chosen), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An Aura can enchant only a creature")
    void cannotEnchantPlayer() {
        harness.setHand(player1, List.of(new KithkinArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("The shield also prevents damage from a spell source")
    void preventsDamageFromSpellSource() {
        Permanent enchanted = addCreatureReady(player1, new DuskriderFalcon());
        Permanent armor = attachArmor(enchanted);
        Thunderbolt thunderbolt = new Thunderbolt();

        harness.setHand(player2, List.of(thunderbolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, enchanted.getId());

        chooseDamageSource(armor, thunderbolt.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Duskrider Falcon");
    }

    @Test
    @DisplayName("The shield prevents combat damage from the chosen attacker")
    void preventsCombatDamageFromChosenAttacker() {
        Permanent enchanted = addCreatureReady(player1, new RedwoodTreefolk());
        Permanent armor = attachArmor(enchanted);
        Permanent attacker = addCreatureReady(player2, new BenalishKnight());

        chooseDamageSource(armor, attacker.getId());

        declareAttackers(player2, List.of(indexOf(player2, attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, enchanted), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private Permanent attachArmor(Permanent enchanted) {
        Permanent aura = new Permanent(new KithkinArmor());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    private void chooseDamageSource(Permanent armor, UUID sourceId) {
        harness.activateAbility(player1, indexOf(player1, armor), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sourceId);
    }
}
