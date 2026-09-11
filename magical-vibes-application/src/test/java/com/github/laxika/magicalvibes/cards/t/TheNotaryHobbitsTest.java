package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TheNotaryHobbits.class)
class TheNotaryHobbitsTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken Notary Hobbits creates two nonlegendary token copies")
    void createsNonlegendaryTokenCopiesOnEntry() {
        harness.setHand(player1, List.of(new TheNotaryHobbits()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> hobbits = findPermanents(player1, "The Notary Hobbits");
        assertThat(hobbits).hasSize(3);
        assertThat(hobbits.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(hobbits.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(permanent -> permanent.getCard().getSupertypes()))
                .allMatch(supertypes -> !supertypes.contains(CardSupertype.LEGENDARY));
    }

    @Test
    @DisplayName("Tapping adds one colorless mana for each Halfling controlled")
    void tappingAddsManaForEachHalfling() {
        harness.setHand(player1, List.of(new TheNotaryHobbits()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent notary = findPermanents(player1, "The Notary Hobbits").stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        notary.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(notary), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }
}
