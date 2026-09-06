package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ExpendableLackey.class)
class ExpendableLackeyTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles Expendable Lackey and creates an unblockable Fish")
    void createsUnblockableFish() {
        ExpendableLackey lackey = new ExpendableLackey();
        harness.setGraveyard(player1, List.of(lackey));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(lackey);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lackey);

        harness.passBothPriorities();

        Permanent fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Fish"))
                .findFirst()
                .orElseThrow();
        assertThat(fish.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(fish.getCard().getSubtypes()).containsExactly(CardSubtype.FISH);
        assertThat(gqs.getEffectivePower(gd, fish)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fish)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, fish)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability can only be activated as a sorcery")
    void abilityIsSorcerySpeedOnly() {
        harness.setGraveyard(player1, List.of(new ExpendableLackey()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
