package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.h.HollowhengeScavenger;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.cards.m.MomentOfHeroism;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscipleOfGriselbrand.class, WalkingCorpse.class, HollowhengeScavenger.class, TyphoidRats.class, MomentOfHeroism.class})
class DiscipleOfGriselbrandTest extends BaseCardTest {


    @Test
    @DisplayName("Sacrificing a 2/2 creature gains 2 life")
    void sacrificing2_2CreatureGains2Life() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        harness.addToBattlefield(player1, new WalkingCorpse());
        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);

        // Walking Corpse should be in graveyard
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Sacrificing a creature with higher toughness gains more life")
    void sacrificingHighToughnessCreatureGainsMoreLife() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        Permanent beefy = addCreatureReady(player1, new HollowhengeScavenger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, beefy.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Sacrificing a 1/1 creature gains 1 life")
    void sacrificing1_1CreatureGains1Life() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        Permanent token = addCreatureReady(player1, new TyphoidRats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Can sacrifice Disciple of Griselbrand to its own ability")
    void canSacrificeItself() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Disciple is 1/1 and auto-sacrificed (only creature), so gains 1 life
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Disciple of Griselbrand");
        harness.assertInGraveyard(player1, "Disciple of Griselbrand");
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());
        Permanent token = addCreatureReady(player1, new TyphoidRats());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());

        // Sacrifice Walking Corpse (toughness 2)
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        // Sacrifice Typhoid Rats (toughness 1) — 2 creatures left (disciple + token)
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        // Total life gained: 2 + 1 = 3
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }


    @Test
    @DisplayName("Requires {1} mana to activate")
    void requiresManaToActivate() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        harness.addToBattlefield(player1, new WalkingCorpse());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Auto-sacrifices Disciple when it is the only creature")
    void autoSacrificesSelfWhenOnlyCreature() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Disciple auto-sacrificed (only creature), gains 1 life (toughness 1)
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Disciple of Griselbrand");
    }


    @Test
    @DisplayName("Uses boosted toughness when the creature is sacrificed")
    void usesBoostedToughnessAtSacrifice() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, corpse.getId());
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, corpse.getId());

        harness.assertInGraveyard(player1, "Walking Corpse");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfGriselbrand());
        disciple.setSummoningSick(true);
        disciple.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Disciple of Griselbrand");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Pending activations retain their own toughness after Disciple is sacrificed")
    void pendingActivationsRetainTheirOwnToughness() {
        addCreatureReady(player1, new DiscipleOfGriselbrand());
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, corpse.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Disciple of Griselbrand");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }
}
