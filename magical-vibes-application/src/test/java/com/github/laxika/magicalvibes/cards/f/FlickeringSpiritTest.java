package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        Permanent spirit = addCreatureReady(player1, new FlickeringSpirit());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Flickering Spirit");
        assertThat(returned.getId()).isNotEqualTo(spirit.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Flickering Spirit"));
    }

    @Test
    @DisplayName("Ability can be activated while Flickering Spirit is tapped")
    void abilityDoesNotRequireUntappedSpirit() {
        Permanent spirit = addCreatureReady(player1, new FlickeringSpirit());
        spirit.tap();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Flickering Spirit").getId()).isNotEqualTo(spirit.getId());
    }

    @Test
    @DisplayName("Ability requires three generic and one white mana")
    void abilityRequiresFourManaIncludingWhite() {
        addCreatureReady(player1, new FlickeringSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability cannot pay its white mana requirement with generic mana alone")
    void abilityRequiresWhiteMana() {
        addCreatureReady(player1, new FlickeringSpirit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability returns an opponent-owned Spirit under its owner's control")
    void abilityReturnsSelfUnderOwnersControl() {
        FlickeringSpirit card = new FlickeringSpirit();
        card.setOwnerId(player2.getId());
        Permanent spirit = addCreatureReady(player1, card);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(spirit.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Flickering Spirit"));
    }
}
