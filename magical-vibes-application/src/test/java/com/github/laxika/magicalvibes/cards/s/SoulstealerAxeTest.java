package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulstealerAxe.class, HillGiant.class, GrayOgre.class, Forest.class, GrizzlyBears.class})
class SoulstealerAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has trample and seeks a card matching its combat damage")
    void equippedCreatureTramplesAndSeeksByCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        axe.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new GrayOgre()));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInHand(player1, "Gray Ogre");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip {2} attaches Soulstealer Axe to a creature you control")
    void equipAttachesToCreature() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }
}
