package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarwynsKindred.class, MarwynTheNurturer.class, LlanowarElves.class})
class MarwynsKindredTest extends BaseCardTest {

    @Test
    void conjuresMarwynAndTheChosenNumberOfLlanowarElves() {
        harness.setHand(player1, List.of(new MarwynsKindred()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);
        assertThat(battlefield).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Marwyn, the Nurturer", "Llanowar Elves", "Llanowar Elves");
        assertThat(battlefield).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void conjuredMarwynRetainsItsAbilities() {
        harness.setHand(player1, List.of(new MarwynsKindred()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Marwyn, the Nurturer"))
                .findFirst().orElseThrow();
        marwyn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
