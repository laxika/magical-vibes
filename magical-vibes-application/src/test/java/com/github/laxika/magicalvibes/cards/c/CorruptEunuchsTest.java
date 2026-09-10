package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptEunuchs.class, Plains.class, ShuFootSoldiers.class})
class CorruptEunuchsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Corrupt Eunuchs puts ETB triggered ability on the stack with the chosen target")
    void resolvingCreaturePutsEtbOnStack() {
        harness.addToBattlefield(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new CorruptEunuchs()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Shu Foot Soldiers");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature spell → ETB triggers

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Corrupt Eunuchs");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB deals 2 damage to target creature and it survives if tough enough")
    void etbDeals2DamageToCreature() {
        ShuFootSoldiers creature = new ShuFootSoldiers();
        creature.setPower(3);
        creature.setToughness(3);
        harness.addToBattlefield(player2, creature);
        harness.setHand(player1, List.of(new CorruptEunuchs()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Shu Foot Soldiers");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        Permanent survivor = findPermanent(player2, "Shu Foot Soldiers");
        assertThat(survivor.getId()).isEqualTo(targetId);
        assertThat(survivor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can target a creature controlled by Corrupt Eunuchs's controller")
    void etbCanTargetControllerCreature() {
        harness.addToBattlefield(player1, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new CorruptEunuchs()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Shu Foot Soldiers");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent target = findPermanent(player1, "Shu Foot Soldiers");
        assertThat(target.getId()).isEqualTo(targetId);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast Corrupt Eunuchs targeting a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new CorruptEunuchs()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB kills a 2-toughness target creature")
    void etbKills2Toughness() {
        ShuFootSoldiers target = new ShuFootSoldiers();
        target.setToughness(2);
        harness.addToBattlefield(player2, target);
        harness.setHand(player1, List.of(new CorruptEunuchs()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Shu Foot Soldiers");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertInGraveyard(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new CorruptEunuchs()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Shu Foot Soldiers");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature — ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // Resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
