package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanDoppelganger.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class VesuvanDoppelgangerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a colorless creature copy and can copy any creature during upkeep")
    void copiesCreaturesWithoutTheirColors() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new VesuvanDoppelganger(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(airElemental.getId(), bears.getId())
                .doesNotContain(findPermanent(player2, "Forest").getId());
        harness.handlePermanentChosen(player1, airElemental.getId());

        Permanent doppelganger = findDoppelganger();
        assertThat(doppelganger.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(gqs.getEffectiveColors(gd, doppelganger)).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(doppelganger.getId(), bears.getId())
                .doesNotContain(findPermanent(player2, "Forest").getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(doppelganger.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, doppelganger)).isEmpty();
    }

    @Test
    @DisplayName("Retains its upkeep copy ability after changing copies")
    void retainsUpkeepCopyAbilityAfterChangingCopies() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new VesuvanDoppelganger(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, airElemental.getId());
        Permanent doppelganger = findDoppelganger();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(doppelganger.getCard().getName()).isEqualTo("Grizzly Bears");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, airElemental.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(doppelganger.getCard().getName()).isEqualTo("Air Elemental");
    }

    private Permanent findDoppelganger() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Vesuvan Doppelganger"))
                .findFirst()
                .orElseThrow();
    }
}
