package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.f.Firebreathing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepentantBlacksmith.class, GrizzlyBears.class, HillGiant.class, Incinerate.class,
        Firebreathing.class, HolyStrength.class, Earthquake.class, WrathOfGod.class})
class RepentantBlacksmithTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Repentant Blacksmith")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new RepentantBlacksmith());
        attacker.setAttacking(true);

        addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Repentant Blacksmith")
    void greenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new RepentantBlacksmith());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Takes no combat damage from red creature")
    void takesNoDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);

        addCreatureReady(player2, new RepentantBlacksmith());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        // Red creature's damage is prevented by protection; Blacksmith survives
        harness.assertOnBattlefield(player2, "Repentant Blacksmith");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent blacksmith = addCreatureReady(player2, new RepentantBlacksmith());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blacksmith.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be enchanted by red Aura")
    void cannotBeEnchantedByRedAura() {
        Permanent blacksmith = addCreatureReady(player1, new RepentantBlacksmith());

        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, blacksmith.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be enchanted by white Aura")
    void canBeEnchantedByWhiteAura() {
        Permanent blacksmith = addCreatureReady(player1, new RepentantBlacksmith());

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, blacksmith.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Protection prevents untargeted red spell damage")
    void preventsUntargetedRedDamage() {
        harness.addToBattlefield(player2, new RepentantBlacksmith());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertOnBattlefield(player2, "Repentant Blacksmith");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Protection from red does not prevent green combat damage")
    void takesCombatDamageFromGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new RepentantBlacksmith());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Repentant Blacksmith");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Protection does not prevent untargeted destruction")
    void diesToMassDestruction() {
        harness.addToBattlefield(player2, new RepentantBlacksmith());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Repentant Blacksmith");
        harness.assertInGraveyard(player2, "Repentant Blacksmith");
    }

    @Test
    @DisplayName("A white Aura resolves and remains attached")
    void whiteAuraResolvesAndRemainsAttached() {
        Permanent blacksmith = addCreatureReady(player1, new RepentantBlacksmith());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, blacksmith.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Holy Strength");
        assertThat(aura.getAttachedTo()).isEqualTo(blacksmith.getId());
        harness.assertOnBattlefield(player1, "Repentant Blacksmith");
    }
}
