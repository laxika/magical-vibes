package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({CabalPit.class, AvenFlock.class, Swamp.class})
class CabalPitTest extends BaseCardTest {

    @Test
    void tapAbilityAddsBlackManaAndDealsDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CabalPit());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Cabal Pit");
    }

    @Test
    void thresholdAbilityGivesCreatureMinusTwoMinusTwoAndSacrificesTheLand() {
        harness.addToBattlefield(player1, new CabalPit());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Cabal Pit");
        harness.assertInGraveyard(player1, "Cabal Pit");
    }

    @Test
    void thresholdAbilityCannotBeActivatedWithFewerThanSevenCardsInGraveyard() {
        harness.addToBattlefield(player1, new CabalPit());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setGraveyard(player1, cards(6));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    void thresholdAbilityCannotTargetALand() {
        harness.addToBattlefield(player1, new CabalPit());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setGraveyard(player1, cards(7));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void manaAbilityCannotBeActivatedWhileTheLandIsTapped() {
        harness.addToBattlefield(player1, new CabalPit());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new CabalPit());
        }
        return cards;
    }
}
