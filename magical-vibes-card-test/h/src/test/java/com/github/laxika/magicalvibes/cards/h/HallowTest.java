package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.f.Flamebreak;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hallow.class, BarbedLightning.class, DarksteelGargoyle.class, Flamebreak.class, ArcboundWorker.class, Triskelion.class})
class HallowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from the targeted spell and gains that much life")
    void preventsTargetedSpellDamageAndGainsLife() {
        BarbedLightning barbedLightning = new BarbedLightning();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(barbedLightning));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{1}, List.of(player1.getId()));
        harness.castAndResolveInstant(player1, 0, barbedLightning.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Prevents damage from the targeted spell to a creature")
    void preventsTargetedSpellDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        BarbedLightning barbedLightning = new BarbedLightning();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(barbedLightning));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(creature.getId()));
        harness.castAndResolveInstant(player1, 0, barbedLightning.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Prevents all damage from the targeted spell and gains life for every prevented damage")
    void preventsAllDamageFromTargetedSpellAndGainsLifeForEveryPreventedDamage() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        Flamebreak flamebreak = new Flamebreak();
        harness.setHand(player1, List.of(flamebreak));
        harness.setHand(player2, List.of(new Hallow()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        harness.castAndResolveInstant(player2, 0, flamebreak.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(29);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Prevention follows a permanent spell and gains life for its ability damage to a player")
    void preventsDamageFromPermanentSpellAfterItResolves() {
        Triskelion triskelion = new Triskelion();
        harness.setHand(player1, List.of(triskelion));
        harness.setHand(player2, List.of(new Hallow()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, triskelion.getId());
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevention follows a permanent spell for repeated ability damage to a creature")
    void preventsRepeatedDamageFromResolvedPermanentToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        Triskelion triskelion = new Triskelion();
        harness.setHand(player1, List.of(triskelion));
        harness.setHand(player2, List.of(new Hallow()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, triskelion.getId());
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, creature.getId());
            harness.passBothPriorities();
        }

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Targeting a spell that deals no damage does not gain life")
    void nonDamagingSpellDoesNotGainLife() {
        ArcboundWorker worker = new ArcboundWorker();
        harness.setHand(player1, List.of(worker));
        harness.setHand(player2, List.of(new Hallow()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, worker.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(worker.getId()));
    }

    @Test
    @DisplayName("Only the targeted spell's damage is prevented")
    void doesNotPreventDamageFromAnotherSpell() {
        BarbedLightning protectedSpell = new BarbedLightning();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(protectedSpell, new BarbedLightning()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player2, 0, 1, List.of(player1.getId()));
        harness.castAndResolveInstant(player1, 0, protectedSpell.getId());
        harness.assertLife(player1, 20);
        harness.castModalInstant(player2, 0, 1, List.of(player1.getId()));
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
