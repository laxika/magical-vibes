package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({UnquestionedAuthority.class, KrosanVerge.class, SuntailHawk.class, GiantWarthog.class})
class UnquestionedAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Unquestioned Authority attaches it and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, hawk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Unquestioned Authority")
                        && hawk.getId().equals(permanent.getAttachedTo()));
        harness.assertInHand(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature")
    void creaturesCannotBlockEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new GiantWarthog());
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent blocker = addCreatureReady(player2, new GiantWarthog());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Unquestioned Authority leaves the battlefield")
    void protectionStopsWhenRemoved() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent aura = enchant(hawk);
        Permanent attacker = addCreatureReady(player2, new GiantWarthog());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, hawk, attacker)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, hawk, attacker)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature takes no combat damage from a creature")
    void enchantedCreatureTakesNoCombatDamageFromCreature() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        hawk.setAttacking(true);
        enchant(hawk);

        Permanent blocker = addCreatureReady(player2, new GiantWarthog());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(hawk.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent land = findPermanent(player1, "Krosan Verge");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new UnquestionedAuthority());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
