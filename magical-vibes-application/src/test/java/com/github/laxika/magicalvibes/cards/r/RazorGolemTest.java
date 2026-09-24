package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorGolem.class, Plains.class, DarksteelCitadel.class})
class RazorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Plains reduces the generic mana cost")
    void affinityForPlainsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        harness.setHand(player1, List.of(new RazorGolem()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only Plains controlled by the spell's controller")
    void affinityCountsOnlyControlledPlains() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Plains());
        }
        harness.setHand(player1, List.of(new RazorGolem()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts tapped Plains and reduces the cost by one per Plains")
    void affinityCountsTappedPlainsAndReducesCostByOnePerPlains() {
        Permanent tappedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        tappedPlains.tap();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        harness.setHand(player1, List.of(new RazorGolem()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity does not count lands without the Plains subtype")
    void affinityDoesNotCountLandsWithoutPlainsSubtype() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new DarksteelCitadel());
        }
        harness.setHand(player1, List.of(new RazorGolem()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Vigilance prevents Razor Golem from tapping when it attacks")
    void vigilancePreventsTappingWhenAttacking() {
        Permanent golem = addCreatureReady(player1, new RazorGolem());

        declareAttackers(List.of(0));

        assertThat(golem.isTapped()).isFalse();
    }
}
