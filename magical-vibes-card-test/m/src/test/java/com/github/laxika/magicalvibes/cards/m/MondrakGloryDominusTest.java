package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BasilicaShepherd;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SinewDancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MondrakGloryDominus.class, BasilicaShepherd.class, SinewDancer.class, PropheticPrism.class})
class MondrakGloryDominusTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles tokens created under its controller's control")
    void doublesTokens() {
        harness.addToBattlefield(player1, new MondrakGloryDominus());
        harness.setHand(player1, List.of(new BasilicaShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mite")).hasSize(4);
    }

    @Test
    @DisplayName("Sacrificing two other artifacts or creatures adds an indestructible counter")
    void sacrificesTwoOtherPermanentsForIndestructibleCounter() {
        Permanent mondrak = harness.addToBattlefieldAndReturn(player1, new MondrakGloryDominus());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SinewDancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, artifact);
        assertThat(mondrak.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mondrak, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot sacrifice Mondrak itself as one of the two permanents")
    void cannotSacrificeSource() {
        harness.addToBattlefield(player1, new MondrakGloryDominus());
        harness.addToBattlefield(player1, new SinewDancer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Does not double tokens created under an opponent's control")
    void doesNotDoubleOpponentTokens() {
        harness.addToBattlefield(player2, new MondrakGloryDominus());
        harness.setHand(player1, List.of(new BasilicaShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mite")).hasSize(2);
        assertThat(findPermanents(player2, "Mite")).isEmpty();
    }

    @Test
    @DisplayName("Two creatures can be sacrificed and both Phyrexian symbols paid with life")
    void sacrificesTwoCreaturesAndPaysFourLife() {
        Permanent mondrak = harness.addToBattlefieldAndReturn(player1, new MondrakGloryDominus());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SinewDancer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SinewDancer());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());

        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(mondrak.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();

        harness.passBothPriorities();

        assertThat(mondrak.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two artifacts can be sacrificed and both Phyrexian symbols paid with white mana")
    void sacrificesTwoArtifactsAndPaysWhiteMana() {
        Permanent mondrak = harness.addToBattlefieldAndReturn(player1, new MondrakGloryDominus());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(mondrak.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Phyrexian symbol can be paid independently with mana or life")
    void paysOneWhiteManaAndTwoLife() {
        Permanent mondrak = harness.addToBattlefieldAndReturn(player1, new MondrakGloryDominus());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SinewDancer());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(mondrak.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot use an opponent's permanent to complete the sacrifice cost")
    void cannotSacrificeOpponentPermanent() {
        harness.addToBattlefield(player1, new MondrakGloryDominus());
        harness.addToBattlefield(player1, new SinewDancer());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }
}
