package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalTorturer.class, CabalCoffers.class, CabalRitual.class, SengirVampire.class})
class CabalTorturerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability gives a target creature -1/-1 until end of turn")
    void firstAbilityShrinksTargetCreature() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(findPermanent(player1, "Cabal Torturer").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The first ability's -1/-1 expires at cleanup")
    void firstAbilityShrinksTargetOnlyUntilEndOfTurn() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);
    }

    @Test
    @DisplayName("The threshold ability gives a target creature -2/-2")
    void thresholdAbilityShrinksTargetCreature() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(findPermanent(player1, "Cabal Torturer").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability cannot be activated below seven graveyard cards")
    void thresholdAbilityRequiresSevenGraveyardCards() {
        addCreatureReady(player1, new CabalTorturer());
        harness.addToBattlefield(player2, new SengirVampire());
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                findPermanent(player2, "Sengir Vampire").getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
        assertThat(findPermanent(player1, "Cabal Torturer").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold ability counts only its controller's graveyard")
    void thresholdAbilityRequiresSevenCardsInControllersGraveyard() {
        addCreatureReady(player1, new CabalTorturer());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setGraveyard(player2, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, vampire.getId()))
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

        Permanent coffers = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, coffers.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, coffers.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(torturer.isTapped()).isFalse();
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new CabalRitual());
        }
        return cards;
    }
}
