package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SarpadianEmpiresVolVII.class)
class SarpadianEmpiresVolVIITest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color stores the selected option")
    void choosingColorStoresSelectedOption() {
        harness.setHand(player1, List.of(new SarpadianEmpiresVolVII()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(artifact.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("The activated ability creates the token paired with the chosen color")
    void activatedAbilityCreatesPairedToken() {
        harness.addToBattlefield(player1, new SarpadianEmpiresVolVII());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        artifact.setChosenColor(CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
    }
}
