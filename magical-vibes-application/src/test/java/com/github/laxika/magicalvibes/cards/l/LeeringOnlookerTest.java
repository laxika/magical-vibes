package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LeeringOnlooker.class)
class LeeringOnlookerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling Leering Onlooker creates two tapped flying Bat tokens")
    void graveyardAbilityCreatesTappedFlyingBats() {
        harness.setGraveyard(player1, List.of(new LeeringOnlooker()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bats = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(bats).hasSize(2);
        assertThat(bats).allSatisfy(bat -> {
            assertThat(bat.isTapped()).isTrue();
            assertThat(bat.getEffectivePower()).isEqualTo(1);
            assertThat(bat.getEffectiveToughness()).isEqualTo(1);
            assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(bat.getCard().getSubtypes()).contains(CardSubtype.BAT);
            assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        harness.assertNotInGraveyard(player1, "Leering Onlooker");
    }
}
