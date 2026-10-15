package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pentavus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiaNalaarChiefMechanic.class, Pentavus.class, GrizzlyBears.class})
class PiaNalaarChiefMechanicTest extends BaseCardTest {

    @Test
    void getsEnergyOnceForMultipleArtifactCreaturesDealingCombatDamage() {
        harness.addToBattlefield(player1, new PiaNalaarChiefMechanic());
        Permanent firstArtifactCreature = addReadyPentavus();
        Permanent secondArtifactCreature = addReadyPentavus();
        Permanent nonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());
        firstArtifactCreature.setAttacking(true);
        secondArtifactCreature.setAttacking(true);
        nonArtifactCreature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void createsVehicleScaledByEnergyPaidAtEndStep() {
        harness.addToBattlefield(player1, new PiaNalaarChiefMechanic());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent aetherjet = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Nalaar Aetherjet"))
                .findFirst()
                .orElseThrow();
        assertThat(aetherjet.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(aetherjet.getCard().getSubtypes()).contains(CardSubtype.VEHICLE);
        assertThat(aetherjet.getCard().hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(aetherjet.getEffectivePower()).isEqualTo(3);
        assertThat(aetherjet.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addReadyPentavus() {
        Permanent pentavus = harness.enterBattlefieldAndReturn(player1, new Pentavus());
        pentavus.setSummoningSick(false);
        return pentavus;
    }
}
