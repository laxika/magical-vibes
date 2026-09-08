package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MerfolkRaiders;
import com.github.laxika.magicalvibes.cards.r.RealityRipple;
import com.github.laxika.magicalvibes.cards.t.TeferisCurse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpatialBinding.class, MerfolkRaiders.class, RealityRipple.class, TeferisCurse.class})
class SpatialBindingTest extends BaseCardTest {

    @Test
    @DisplayName("Protected permanent does not phase out during its controller's untap step")
    void protectedPermanentSkipsUntapStepPhasing() {
        harness.addToBattlefield(player1, new SpatialBinding());
        harness.addToBattlefield(player2, new MerfolkRaiders());

        activateBinding(harness.getPermanentId(player2, "Merfolk Raiders"));

        advanceTurn(player2); // player2's untap step — Merfolk Raiders would normally phase out
        harness.assertOnBattlefield(player2, "Merfolk Raiders");
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).isEmpty();
    }

    @Test
    @DisplayName("Activating costs 1 life")
    void activationPaysOneLife() {
        harness.addToBattlefield(player1, new SpatialBinding());
        harness.addToBattlefield(player2, new MerfolkRaiders());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activateBinding(harness.getPermanentId(player2, "Merfolk Raiders"));

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("A protected permanent can't be phased out by an effect either")
    void protectedPermanentResistsPhaseOutEffect() {
        harness.addToBattlefield(player1, new SpatialBinding());
        harness.addToBattlefield(player2, new MerfolkRaiders());
        UUID targetId = harness.getPermanentId(player2, "Merfolk Raiders");

        activateBinding(targetId);

        harness.setHand(player1, List.of(new RealityRipple()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Merfolk Raiders");
    }

    @Test
    @DisplayName("The restriction ends at the controller's next upkeep, so the permanent phases out afterwards")
    void restrictionEndsAtNextUpkeep() {
        harness.addToBattlefield(player1, new SpatialBinding());
        harness.addToBattlefield(player2, new MerfolkRaiders());

        activateBinding(harness.getPermanentId(player2, "Merfolk Raiders"));

        advanceTurn(player2); // player2's untap step — still protected
        harness.assertOnBattlefield(player2, "Merfolk Raiders");
        advanceTurn(player1); // player1's untap step clears the restriction at upkeep
        advanceTurn(player2); // player2's untap step — now it phases out
        harness.assertNotOnBattlefield(player2, "Merfolk Raiders");
    }

    @Test
    @DisplayName("A later activation does not replace an earlier player's restriction")
    void overlappingActivationsKeepBothRestrictions() {
        harness.addToBattlefield(player1, new SpatialBinding());
        harness.addToBattlefield(player2, new SpatialBinding());
        harness.addToBattlefield(player1, new MerfolkRaiders());
        UUID targetId = harness.getPermanentId(player1, "Merfolk Raiders");

        activateBinding(player1, targetId);
        activateBinding(player2, targetId);

        advanceTurn(player2);
        advanceTurn(player1);

        harness.assertOnBattlefield(player1, "Merfolk Raiders");
    }

    @Test
    @DisplayName("A protected Aura is put into the graveyard when its host phases out without it")
    void protectedAuraIsOrphanedWhenItsHostPhasesOut() {
        harness.addToBattlefield(player1, new SpatialBinding());
        harness.addToBattlefield(player2, new MerfolkRaiders());
        Permanent raiders = findPermanent(player2, "Merfolk Raiders");
        Permanent curse = new Permanent(new TeferisCurse());
        curse.setAttachedTo(raiders.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        activateBinding(curse.getId());

        advanceTurn(player2); // player2's untap step — the Raiders phase out, the Curse can't follow

        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).contains(raiders);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).doesNotContain(curse);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(curse);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Teferi's Curse"));
    }

    private void activateBinding(UUID targetId) {
        activateBinding(player1, targetId);
    }

    private void activateBinding(Player controller, UUID targetId) {
        harness.activateAbility(controller, 0, null, targetId);
        harness.passBothPriorities();
    }

    private void advanceTurn(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }

}
