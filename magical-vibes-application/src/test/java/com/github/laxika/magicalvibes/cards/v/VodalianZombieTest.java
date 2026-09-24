package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AggressiveUrge;
import com.github.laxika.magicalvibes.cards.p.PincerSpider;
import com.github.laxika.magicalvibes.cards.w.WhipSilk;
import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({VodalianZombie.class, AggressiveUrge.class, PincerSpider.class, WhipSilk.class, Zap.class})
class VodalianZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Vodalian Zombie has protection from green")
    void hasProtectionFromGreen() {
        harness.addToBattlefield(player1, new VodalianZombie());
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(gqs.hasProtectionFrom(gd, zombie, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, zombie, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Vodalian Zombie cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new VodalianZombie());
        harness.addToBattlefield(player2, new PincerSpider());
        harness.setHand(player1, List.of(new AggressiveUrge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has protection from green");
    }

    @Test
    @DisplayName("Vodalian Zombie can be targeted by a red spell")
    void canBeTargetedByRedSpell() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new VodalianZombie());
        harness.setLibrary(player1, List.of(new PincerSpider()));
        harness.setHand(player1, List.of(new Zap()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, zombie.getId());

        assertThat(zombie.getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Pincer Spider");
    }

    @Test
    @DisplayName("A green creature cannot block Vodalian Zombie")
    void greenCreatureCannotBlock() {
        addCreatureReady(player1, new VodalianZombie());
        addCreatureReady(player2, new PincerSpider());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from green prevents combat damage from a green creature")
    void preventsCombatDamageFromGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new PincerSpider());
        Permanent zombie = addCreatureReady(player2, new VodalianZombie());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(zombie.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Vodalian Zombie cannot be enchanted by a green Aura")
    void cannotBeEnchantedByGreenAura() {
        Permanent zombie = addCreatureReady(player2, new VodalianZombie());
        addCreatureReady(player2, new PincerSpider());
        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has protection from green");
    }
}
