package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CentaurGardenTest extends BaseCardTest {

    @Test
    void tapAbilityAddsGreenManaAndDealsDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CentaurGarden());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Centaur Garden");
    }

    @Test
    void thresholdAbilityGivesCreaturePlusThreePlusThreeAndSacrificesTheLand() {
        harness.addToBattlefield(player1, new CentaurGarden());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(7);
        harness.assertNotOnBattlefield(player1, "Centaur Garden");
        harness.assertInGraveyard(player1, "Centaur Garden");
    }

    @Test
    void thresholdAbilityCannotBeActivatedWithFewerThanSevenCardsInGraveyard() {
        harness.addToBattlefield(player1, new CentaurGarden());
        harness.setGraveyard(player1, cards(6));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    void thresholdAbilityCannotTargetALand() {
        harness.addToBattlefield(player1, new CentaurGarden());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new CentaurGarden());
        }
        return cards;
    }
}
