package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.g.GrindingStation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IdrisSoulOfTheTARDIS.class, ConjurersBauble.class, GrindingStation.class,
        GrizzlyBears.class})
class IdrisSoulOfTheTARDISTest extends BaseCardTest {

    @Test
    void exilesArtifactAndGetsItsManaValueAndActivatedAbility() {
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
        castIdris(bauble.getId());

        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        assertThat(findPermanents(player1, "Conjurer's Bauble")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, idris)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, idris)).isEqualTo(4);

        idris.setSummoningSick(false);
        var graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Idris, Soul of the TARDIS")).isEmpty();
        assertThat(findPermanents(player1, "Conjurer's Bauble")).hasSize(1);
    }

    @Test
    void gainsTriggeredAbilityOfExiledArtifact() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        station.tap();
        castIdris(station.getId());
        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        idris.tap();

        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(idris.isTapped()).isFalse();
    }

    @Test
    void rejectsNonArtifactTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IdrisSoulOfTheTARDIS()));
        addIdrisMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private void castIdris(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new IdrisSoulOfTheTARDIS()));
        addIdrisMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addIdrisMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
