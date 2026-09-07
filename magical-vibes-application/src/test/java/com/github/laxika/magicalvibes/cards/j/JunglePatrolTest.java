package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JunglePatrol.class})
class JunglePatrolTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 0/1 green Wall token with defender named Wood")
    void createsWoodToken() {
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wood = findPermanent(player1, "Wood");
        assertThat(wood.getCard().getPower()).isEqualTo(0);
        assertThat(wood.getCard().getToughness()).isEqualTo(1);
        assertThat(wood.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(wood.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("First ability creates a green Wall creature token named Wood and taps Jungle Patrol")
    void createsCorrectWoodTokenCharacteristics() {
        Permanent patrol = addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wood = findPermanent(player1, "Wood");
        assertThat(patrol.isTapped()).isTrue();
        assertThat(wood.getCard().getName()).isEqualTo("Wood");
        assertThat(wood.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wood.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wood.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("Second ability sacrifices a Wood token to add {R}")
    void sacrificesWoodForRedMana() {
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wood");

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Wood");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability cannot be activated without a Wood token")
    void requiresWoodToken() {
        addCreatureReady(player1, new JunglePatrol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
