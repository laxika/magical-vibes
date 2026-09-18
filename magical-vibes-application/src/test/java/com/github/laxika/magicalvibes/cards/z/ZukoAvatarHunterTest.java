package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZukoAvatarHunter.class, Shock.class, Divination.class})
class ZukoAvatarHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a red spell creates a 2/2 red Soldier token")
    void redSpellCreatesSoldierToken() {
        harness.addToBattlefield(player1, new ZukoAvatarHunter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a non-red spell does not create a Soldier token")
    void nonRedSpellDoesNotCreateSoldierToken() {
        harness.addToBattlefield(player1, new ZukoAvatarHunter());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }
}
