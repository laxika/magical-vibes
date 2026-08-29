package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormlessNurturingTest extends BaseCardTest {

    @Test
    void manifestsTopCardAndPutsOnePlusOneCounterOnIt() {
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new FormlessNurturing()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);
    }

    @Test
    void doesNothingWhenItsLibraryIsEmpty() {
        harness.setHand(player1, List.of(new FormlessNurturing()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }
}
