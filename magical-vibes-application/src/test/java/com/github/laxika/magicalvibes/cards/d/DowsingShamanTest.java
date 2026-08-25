package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DowsingShaman.class, Cessation.class, GrizzlyBears.class})
class DowsingShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target enchantment card from the graveyard to its controller's hand")
    void returnsTargetEnchantmentToHand() {
        Permanent shaman = addReadyDowsingShaman();
        Card enchantment = new Cessation();
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-enchantment card in the graveyard")
    void cannotTargetNonEnchantmentCard() {
        Permanent shaman = addReadyDowsingShaman();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    private Permanent addReadyDowsingShaman() {
        Permanent shaman = new Permanent(new DowsingShaman());
        shaman.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(shaman);
        return shaman;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
