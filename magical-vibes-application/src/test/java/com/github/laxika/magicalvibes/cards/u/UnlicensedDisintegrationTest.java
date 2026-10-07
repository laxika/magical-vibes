package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.FairgroundsWarden;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnlicensedDisintegration.class, RodOfRuin.class, GrizzlyBears.class, OmegaMyr.class,
        DarksteelMyr.class, ConsulateSkygate.class, FairgroundsWarden.class})
class UnlicensedDisintegrationTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureAndDealsDamageWhenArtifactIsControlled() {
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    void destroysTargetCreatureWithoutDamageWhenNoArtifactIsControlled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    void checksForArtifactAfterDestroyingTarget() {
        harness.addToBattlefield(player1, new OmegaMyr());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Omega Myr"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Omega Myr");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void dealsDamageEvenWhenTargetCannotBeDestroyed() {
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addToBattlefield(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Darksteel Myr"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Rod of Ruin")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentArtifactDoesNotEnableDamage() {
        harness.addToBattlefield(player2, new ConsulateSkygate());
        harness.addToBattlefield(player2, new ConsulateSkygate());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Consulate Skygate"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Consulate Skygate");
        harness.assertOnBattlefield(player2, "Consulate Skygate");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void destroyingOwnCreatureDealsDamageToCasterWhenAnotherArtifactRemains() {
        harness.addToBattlefield(player1, new ConsulateSkygate());
        harness.addToBattlefield(player1, new ConsulateSkygate());
        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Consulate Skygate"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Consulate Skygate");
        harness.assertOnBattlefield(player1, "Consulate Skygate");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void illegalTargetPreventsAdditionalDamage() {
        harness.addToBattlefield(player1, new ConsulateSkygate());
        harness.addToBattlefield(player2, new ConsulateSkygate());
        harness.setHand(player1, List.of(new UnlicensedDisintegration(), new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        var targetId = harness.getPermanentId(player2, "Consulate Skygate");

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Consulate Skygate");
        harness.assertLife(player2, 17);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Unlicensed Disintegration");
    }

    @Test
    void returningArtifactDuringDestructionEnablesDamage() {
        harness.addToBattlefield(player1, new ConsulateSkygate());
        harness.setHand(player2, List.of(new FairgroundsWarden()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, harness.getPermanentId(player1, "Consulate Skygate"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Consulate Skygate");

        harness.setHand(player1, List.of(new UnlicensedDisintegration()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fairgrounds Warden"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fairgrounds Warden");
        harness.assertOnBattlefield(player1, "Consulate Skygate");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }
}
