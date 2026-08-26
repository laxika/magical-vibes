package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FlickeringSpirit.class)
class FlickeringSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles Flickering Spirit and immediately returns it")
    void abilityFlickersSelf() {
        Permanent spirit = addReadySpirit(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Flickering Spirit");
        assertThat(returned.getId()).isNotEqualTo(spirit.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Flickering Spirit"));
    }

    @Test
    @DisplayName("Ability requires three generic and one white mana")
    void abilityRequiresFourManaIncludingWhite() {
        addReadySpirit(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadySpirit(Player player) {
        Permanent spirit = new Permanent(new FlickeringSpirit());
        spirit.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spirit);
        return spirit;
    }
}
