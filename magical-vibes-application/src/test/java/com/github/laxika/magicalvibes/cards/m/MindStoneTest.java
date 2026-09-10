package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MindStone.class)
class MindStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mind Stone produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent mindStone = addReadyMindStone();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(mindStone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying one mana and sacrificing Mind Stone draws a card")
    void sacrificingDrawsACard() {
        Permanent mindStone = addReadyMindStone();
        MindStone drawnCard = new MindStone();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mindStone);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mindStone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Mind Stone cannot be sacrificed without paying its mana cost")
    void sacrificeAbilityRequiresOneMana() {
        Permanent mindStone = addReadyMindStone();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mindStone);
        assertThat(mindStone.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Mind Stone's draw ability cannot be activated while it is tapped")
    void sacrificeAbilityRequiresUntappedStone() {
        Permanent mindStone = addReadyMindStone();
        mindStone.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mindStone);
        harness.assertNotInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyMindStone() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        mindStone.setSummoningSick(false);
        return mindStone;
    }
}
