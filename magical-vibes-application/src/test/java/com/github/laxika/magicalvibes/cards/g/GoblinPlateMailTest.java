package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinPlateMail.class, GrizzlyBears.class})
class GoblinPlateMailTest extends BaseCardTest {

    @Test
    void entersAttachedToNewlyAmassedArmy() {
        castPlateMail();

        Permanent army = findPermanent(player1, "Goblin Army");
        Permanent plateMail = findPermanent(player1, "Goblin Plate Mail");

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
        assertThat(plateMail.getAttachedTo()).isEqualTo(army.getId());
        assertThat(gqs.getEffectivePower(gd, army)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, army)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isTrue();
    }

    @Test
    void entersAttachedToExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castPlateMail();

        Permanent plateMail = findPermanent(player1, "Goblin Plate Mail");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(plateMail.getAttachedTo()).isEqualTo(army.getId());
        assertThat(gqs.getEffectivePower(gd, army)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, army)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isTrue();
    }

    @Test
    void canEquipAnotherCreature() {
        Permanent plateMail = harness.addToBattlefieldAndReturn(player1, new GoblinPlateMail());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(plateMail.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    private void castPlateMail() {
        harness.setHand(player1, List.of(new GoblinPlateMail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
