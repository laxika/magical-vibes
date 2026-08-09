package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LuminousAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may create a 1/1 white Spirit token with flying")
    void upkeepCreatesSpiritToken() {
        harness.addToBattlefield(player1, new LuminousAngel());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Declining the upkeep trigger creates no token")
    void upkeepDeclinedCreatesNoToken() {
        harness.addToBattlefield(player1, new LuminousAngel());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Luminous Angel does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new LuminousAngel());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }
}
