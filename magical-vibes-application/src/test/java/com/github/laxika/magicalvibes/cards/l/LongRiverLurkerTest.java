package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LongRiverLurker.class, Frogmite.class, GrizzlyBears.class, Shock.class})
class LongRiverLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Long River Lurker's ward protects itself from an unpaid opponent spell")
    void wardProtectsItself() {
        Permanent lurker = addCreatureReady(player1, new LongRiverLurker());

        castOpponentShock(lurker);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Long River Lurker");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Other Frogs you control have ward")
    void grantsWardToOtherFrogs() {
        addCreatureReady(player1, new LongRiverLurker());
        Permanent frogmite = addCreatureReady(player1, new Frogmite());

        castOpponentShock(frogmite);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frogmite");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("ETB target cannot be blocked and may be flickered after dealing combat damage")
    void targetBecomesUnblockableAndMayBeFlickered() {
        Permanent bears = castLurkerTargetingBears();
        UUID oldId = bears.getId();
        bears.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(oldId);
        assertThat(returned.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The combat-damage flicker may be declined")
    void mayDeclineCombatDamageFlicker() {
        Permanent bears = castLurkerTargetingBears();
        UUID oldId = bears.getId();
        bears.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent remaining = findPermanent(player1, "Grizzly Bears");
        assertThat(remaining.getId()).isEqualTo(oldId);
    }

    private Permanent castLurkerTargetingBears() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LongRiverLurker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
        return bears;
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
