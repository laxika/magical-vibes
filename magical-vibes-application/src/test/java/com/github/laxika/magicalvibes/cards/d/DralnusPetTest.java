package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DralnusPet.class, ArcticMerfolk.class, MeteorCrater.class})
class DralnusPetTest extends BaseCardTest {

    @Test
    void entersWithoutKickerBenefits() {
        ArcticMerfolk retained = new ArcticMerfolk();
        harness.setHand(player1, List.of(new DralnusPet(), retained));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pet = findPermanent(player1, "Dralnu's Pet");
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, pet, Keyword.FLYING)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }

    @Test
    void kickedCreatureIsDiscardedAndItsManaValueBecomesCounters() {
        ArcticMerfolk discarded = new ArcticMerfolk();
        harness.setHand(player1, List.of(new DralnusPet(), discarded));
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), true, 1);
        harness.passBothPriorities();

        Permanent pet = findPermanent(player1, "Dralnu's Pet");
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pet, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void kickedCastRejectsNonCreatureDiscard() {
        DralnusPet pet = new DralnusPet();
        MeteorCrater discarded = new MeteorCrater();
        harness.setHand(player1, List.of(pet, discarded));
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), true, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pet, discarded);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

}
