package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiminishedReturner.class, Shock.class})
class DiminishedReturnerTest extends BaseCardTest {

    @Test
    void entersTapped() {
        DiminishedReturner returner = new DiminishedReturner();
        harness.setGraveyard(player1, List.of(returner));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, graveyardIndex(returner));
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, returner);
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
    }

    @Test
    void perpetualToughnessRestrictionStopsActivationBelowTwo() {
        DiminishedReturner returner = new DiminishedReturner();
        harness.setGraveyard(player1, List.of(returner));
        activateAndReturn(returner);

        destroyReturner(returner);
        activateAndReturn(returner);

        Permanent permanent = findPermanent(player1, returner);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);

        destroyReturner(returner);
        harness.addMana(player1, ManaColor.BLACK, 2);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, graveyardIndex(returner)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness is 2 or greater");
    }

    private void activateAndReturn(DiminishedReturner returner) {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, graveyardIndex(returner));
        harness.passBothPriorities();
        assertThat(findPermanent(player1, returner)).isNotNull();
    }

    private void destroyReturner(DiminishedReturner returner) {
        Permanent permanent = findPermanent(player1, returner);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, DiminishedReturner returner) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() == returner)
                .findFirst()
                .orElseThrow();
    }

    private int graveyardIndex(DiminishedReturner returner) {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = gd.playerGraveyards.get(player1.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i) == returner) {
                return i;
            }
        }
        throw new AssertionError("Diminished Returner is not in the graveyard");
    }
}
