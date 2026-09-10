package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodswornSquire.class, BloodswornKnight.class, GrizzlyBears.class, Shock.class})
class BloodswornSquireTest extends BaseCardTest {

    @Test
    void abilityTapsAndGrantsIndestructibleWithoutTransformingBelowThreshold() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(squire.isTransformed()).isFalse();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void transformsWhenFourCreatureCardsAreInGraveyard() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isTrue();
        assertThat(squire.getCard().getName()).isEqualTo("Bloodsworn Knight");
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(4);
    }

    @Test
    void backFaceAbilityTapsAndGrantsIndestructible() {
        BloodswornSquire card = new BloodswornSquire();
        Permanent knight = new Permanent(card);
        knight.setCard(card.getBackFaceCard());
        knight.setTransformed(true);
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(knight);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(knight.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(knight.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    private Permanent addSquireReady() {
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new BloodswornSquire());
        squire.setSummoningSick(false);
        return squire;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
