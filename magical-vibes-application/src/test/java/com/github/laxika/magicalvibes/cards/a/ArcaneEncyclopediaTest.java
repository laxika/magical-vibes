package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneEncyclopedia.class, Forest.class})
class ArcaneEncyclopediaTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T} draws a card")
    void drawsACard() {
        Permanent encyclopedia = addEncyclopedia();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(encyclopedia.isTapped()).isTrue();
    }

    @Test
    void paysCostsImmediatelyAndDrawsOnlyOnResolution() {
        Permanent encyclopedia = addEncyclopedia();
        encyclopedia.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player1, List.of());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);

        assertThat(encyclopedia.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void cannotActivateWithoutThreeMana() {
        Permanent encyclopedia = addEncyclopedia();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(encyclopedia.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent encyclopedia = addEncyclopedia();
        encyclopedia.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent encyclopedia = addEncyclopedia();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(encyclopedia);
        harness.setGraveyard(player1, List.of(encyclopedia.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private Permanent addEncyclopedia() {
        return harness.addToBattlefieldAndReturn(player1, new ArcaneEncyclopedia());
    }
}
