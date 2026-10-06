package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivanFire.class, GrizzlyBears.class, HillGiant.class, PrimordialWurm.class})
class ShivanFireTest extends BaseCardTest {

    @Test
    void deals2DamageToTargetCreatureUnkicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Hill Giant is 3/3 — survives 2 damage
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hill Giant should still be on the battlefield with 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        Permanent survivingGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(giant.getId()))
                .findFirst().orElseThrow();
        assertThat(survivingGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void unkickedKillsCreatureWith2OrLessToughness() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Grizzly Bears is 2/2 — dies to 2 damage
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void kickedDeals4DamageToTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        // Base cost {R} + kicker {4} = 5 mana total
        harness.addMana(player1, ManaColor.RED, 5);

        // Hill Giant is 3/3 — dies to 4 damage
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castKickedInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shivan Fire");
    }

    @Test
    void kickedDealsExactlyFourDamageToOwnCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());

        harness.castKickedInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primordial Wurm");
        assertThat(wurm.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Shivan Fire");
    }

    @Test
    void canDeclineKickerWithEnoughManaToPayIt() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 5);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotKickWithoutPayingTheAdditionalFourMana() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    void cannotTargetPlayerWhenUnkicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetPlayerWhenKicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
