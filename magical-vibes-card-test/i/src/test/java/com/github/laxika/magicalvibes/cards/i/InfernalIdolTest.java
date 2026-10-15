package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernalIdol.class, Plains.class})
class InfernalIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Infernal Idol produces one black mana")
    void tappingProducesBlackMana() {
        Permanent idol = addReadyInfernalIdol();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(idol.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability requires one colorless and two black mana")
    void drawAbilityRequiresExactManaCost() {
        addReadyInfernalIdol();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Infernal Idol draws two cards and loses 2 life")
    void sacrificingDrawsTwoAndLosesLife() {
        Permanent idol = addReadyInfernalIdol();
        Plains firstCard = new Plains();
        Plains secondCard = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(idol);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(idol.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyInfernalIdol() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new InfernalIdol());
        idol.setSummoningSick(false);
        return idol;
    }
}
