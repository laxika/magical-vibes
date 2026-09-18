package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed(DeathByDragons.class)
class DeathByDragonsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player other than the target creates a 5/5 flying red Dragon")
    void createsDragonsForPlayersOtherThanTarget() {
        harness.setHand(player1, List.of(new DeathByDragons()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Dragon")).isEmpty();
        List<Permanent> dragons = findPermanents(player1, "Dragon");
        assertThat(dragons).hasSize(1);
        Permanent dragon = dragons.getFirst();
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getPower()).isEqualTo(5);
        assertThat(dragon.getCard().getToughness()).isEqualTo(5);
        assertThat(dragon.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(dragon.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("May target the controller, leaving only the other player without a Dragon")
    void targetingControllerExcludesController() {
        harness.setHand(player1, List.of(new DeathByDragons()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
        assertThat(findPermanents(player2, "Dragon")).hasSize(1);
    }
}
