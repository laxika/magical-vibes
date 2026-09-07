package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkullProphet.class)
class SkullProphetTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds black mana when black is chosen")
    void manaAbilityAddsBlackMana() {
        Permanent prophet = addReadyProphet();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(prophet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds green mana when green is chosen")
    void manaAbilityAddsGreenMana() {
        Permanent prophet = addReadyProphet();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(prophet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mill ability puts the top two cards of its controller's library into their graveyard")
    void millAbilityMillsTwoCards() {
        Permanent prophet = addReadyProphet();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(prophet.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private Permanent addReadyProphet() {
        return addCreatureReady(player1, new SkullProphet());
    }
}
