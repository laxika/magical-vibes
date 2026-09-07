package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalTorturer.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class CabalTorturerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability gives a target creature -1/-1 until end of turn")
    void firstAbilityShrinksTargetCreature() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(findPermanent(player1, "Cabal Torturer").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability gives a target creature -2/-2")
    void thresholdAbilityShrinksTargetCreature() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold ability cannot be activated below seven graveyard cards")
    void thresholdAbilityRequiresSevenGraveyardCards() {
        addCreatureReady(player1, new CabalTorturer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                findPermanent(player2, "Grizzly Bears").getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    @DisplayName("Both abilities only target creatures")
    void abilitiesCannotTargetNoncreatures() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent torturer = findPermanent(player1, "Cabal Torturer");
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(torturer.isTapped()).isFalse();
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
