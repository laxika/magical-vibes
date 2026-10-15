package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BehemothSledge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.cards.s.SarkhanVol;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HissingIguanar.class, GrizzlyBears.class, Shock.class, JundCharm.class, SarkhanVol.class, BehemothSledge.class})
class HissingIguanarTest extends BaseCardTest {

    // "Whenever another creature dies, you may have this creature deal 1 damage to
    //  target player or planeswalker."

    /** Player1 shocks the named creature; resolve Shock and its death. */
    private void killWithShock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Accepting deals 1 damage to the chosen target when another creature dies")
    void acceptingDealsDamage() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Grizzly Bears"));
        // CR 603.3d: a targeted "may" ability chooses its target as it is put on the stack; the
        // "may" decision is made when it resolves.
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the death trigger -> "may" prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the death trigger -> "may" prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("The source's own death does not trigger the ability (\"another creature\")")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new HissingIguanar());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Hissing Iguanar"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("An opposing creature dying triggers damage and the controller may target themself")
    void opposingCreatureDeathCanDamageController() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damage can target a planeswalker and removes one loyalty")
    void damagesPlaneswalker() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SarkhanVol());
        var planeswalker = gd.playerBattlefields.get(player2.getId()).getFirst();
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);
        int lifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once per other creature even when Hissing Iguanar dies")
    void simultaneousDeathsTriggerForEachOtherCreature() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JundCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Hissing Iguanar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lifelink granted by equipment applies to the triggered damage")
    void triggeredDamageUsesGrantedLifelink() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new BehemothSledge());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 1, null, harness.getPermanentId(player1, "Hissing Iguanar"));
        harness.passBothPriorities();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
    }
}
