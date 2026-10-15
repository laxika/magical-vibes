package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.v.VampiresZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({FirecannonBlast.class, AirElemental.class, GrizzlyBears.class, ColossalDreadmaw.class, VampiresZeal.class})
class FirecannonBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature without raid")
    void deals3DamageWithoutRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // 3 damage kills a 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals only 3 damage without raid Ă˘â‚¬â€ť 4/4 creature survives")
    void fourToughnessCreatureSurvivesWithoutRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // 3 damage does not kill a 4/4
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Deals 6 damage with raid Ă˘â‚¬â€ť kills a 4/4 creature")
    void deals6DamageWithRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        // Simulate having attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // 6 damage kills a 4/4
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Opponent attacking does not enable raid for caster")
    void opponentAttackingDoesNotEnableRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        // Opponent attacked, not the caster
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // Only 3 damage Ă˘â‚¬â€ť 4/4 survives
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Raid is checked at resolution time, not cast time")
    void raidCheckedAtResolution() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        // No raid when casting
        harness.castSorcery(player1, 0, targetId);

        // Raid becomes active after casting but before resolution
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.passBothPriorities();

        // Should deal 6 since raid is met at resolution time
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Raid deals enough damage to kill a six-toughness creature")
    void killsSixToughnessCreatureWithRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        var target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Can target its controller's creature and marks exactly three damage without raid")
    void canDamageOwnCreatureWithoutRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        var target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Air Elemental");
        org.assertj.core.api.Assertions.assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Raid replaces three damage with exactly six damage")
    void raidDealsExactlySixDamage() {
        harness.setHand(player1, List.of(new VampiresZeal(), new FirecannonBlast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        var target = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.castAndResolveInstant(player1, 0, target.getId());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        org.assertj.core.api.Assertions.assertThat(target.getMarkedDamage()).isEqualTo(6);
    }
}
