package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullOfOrm.class, Pacifism.class, GrizzlyBears.class})
class SkullOfOrmTest extends BaseCardTest {

    @Test
    @DisplayName("{5}, {T}: Return target enchantment card from graveyard to hand")
    void returnEnchantmentFromGraveyard() {
        harness.addToBattlefield(player1, new SkullOfOrm());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Card pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));

        harness.activateAbility(player1, 0, 0, null, pacifism.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment card")
    void cannotReturnNonEnchantment() {
        harness.addToBattlefield(player1, new SkullOfOrm());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsEnchantment() {
        harness.addToBattlefield(player1, new SkullOfOrm());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Card pacifism = new Pacifism();
        harness.setGraveyard(player2, List.of(pacifism));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, pacifism.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
