package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulOfNewPhyrexia;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianScrapyard.class, GrizzlyBears.class, SoulOfNewPhyrexia.class})
class PhyrexianScrapyardTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent scrapyard = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(scrapyard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Discarding a card conjures Phyrexian Scrapyard into hand")
    void discardingConjuresScrapyard() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Phyrexian Scrapyard");
    }

    @Test
    @DisplayName("Sacrificing three Scrapyards conjures Soul of New Phyrexia")
    void sacrificingThreeScrapyardsConjuresSoul() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Phyrexian Scrapyard"))
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SoulOfNewPhyrexia);
    }

    @Test
    @DisplayName("The Soul of New Phyrexia ability is sorcery speed")
    void soulAbilityIsSorcerySpeed() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
