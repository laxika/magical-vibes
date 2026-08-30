package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanDoppelganger.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class VesuvanDoppelgangerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a colorless creature copy and can copy another creature during upkeep")
    void copiesCreaturesWithoutTheirColors() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VesuvanDoppelganger()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(airElemental.getId(), bears.getId())
                .doesNotContain(gd.playerBattlefields.get(player2.getId()).stream()
                        .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                        .findFirst()
                        .orElseThrow()
                        .getId());
        harness.handlePermanentChosen(player1, airElemental.getId());

        Permanent doppelganger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Vesuvan Doppelganger"))
                .findFirst()
                .orElseThrow();
        assertThat(doppelganger.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(gqs.getEffectiveColors(gd, doppelganger)).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(gd.playerBattlefields.get(player2.getId()).stream()
                        .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                        .findFirst()
                        .orElseThrow()
                        .getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(doppelganger.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, doppelganger)).isEmpty();
    }
}
