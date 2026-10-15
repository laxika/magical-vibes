package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianRing.class, DwarvenGrunt.class})
class BarbarianRingTest extends BaseCardTest {

    @Test
    void tapAbilityAddsRedManaAndDealsDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BarbarianRing());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Barbarian Ring");
    }

    @Test
    void thresholdAbilityDealsTwoDamageAndSacrificesTheLand() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Barbarian Ring");
        harness.assertInGraveyard(player1, "Barbarian Ring");
    }

    @Test
    void thresholdAbilityDealsTwoDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dwarven Grunt");
        harness.assertInGraveyard(player1, "Barbarian Ring");
    }

    @Test
    void thresholdAbilityCannotBeActivatedWithFewerThanSevenCardsInGraveyard() {
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(6));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    void manaAbilityCannotBeActivatedWhileTheLandIsTapped() {
        harness.addToBattlefield(player1, new BarbarianRing());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void thresholdAbilityPaysCostsBeforeResolving() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Barbarian Ring");
        harness.assertInGraveyard(player1, "Barbarian Ring");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void thresholdIsNotCheckedAgainOnResolution() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsGraveyardDoesNotSatisfyThreshold() {
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(6));
        harness.setGraveyard(player2, cards(7));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");

        harness.assertOnBattlefield(player1, "Barbarian Ring");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void thresholdAbilityCannotUseManaFromTheSameTappedRing() {
        harness.addToBattlefield(player1, new BarbarianRing());
        harness.setGraveyard(player1, cards(7));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player1, "Barbarian Ring");
    }

    @Test
    void thresholdAbilityCannotTargetANoncreatureLand() {
        harness.addToBattlefield(player1, new BarbarianRing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarbarianRing());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Barbarian Ring");
        harness.assertOnBattlefield(player2, "Barbarian Ring");
        assertThat(gd.stack).isEmpty();
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new BarbarianRing());
        }
        return cards;
    }
}
