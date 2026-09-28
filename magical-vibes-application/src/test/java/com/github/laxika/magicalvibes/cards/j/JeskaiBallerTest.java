package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed(JeskaiBaller.class)
class JeskaiBallerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Athlete token when cast and rebounds from hand")
    void createsTokenAndReboundsFromHand() {
        JeskaiBaller baller = castFromHand();

        assertThat(athleteTokens()).hasSize(1);
        assertThat(athleteTokens().getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(athleteTokens().getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.ATHLETE);
        assertThat(gqs.getEffectivePower(gd, athleteTokens().getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, athleteTokens().getFirst())).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Jeskai Baller");
        assertThat(gd.findExiledCard(baller.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(athleteTokens()).hasSize(2);
        harness.assertOnBattlefield(player1, "Jeskai Baller");
        assertThat(gd.findExiledCard(baller.getId())).isNull();
    }

    @Test
    @DisplayName("The cast trigger resolves before the rebounding creature spell")
    void castTriggerResolvesBeforeCreatureSpell() {
        JeskaiBaller baller = new JeskaiBaller();
        harness.setHand(player1, List.of(baller));
        addBallerMana();

        harness.castCreature(player1, 0);

        assertThat(athleteTokens()).isEmpty();
        harness.passBothPriorities();
        assertThat(athleteTokens()).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Jeskai Baller");
        assertThat(gd.findExiledCard(baller.getId())).isNull();
    }

    private JeskaiBaller castFromHand() {
        JeskaiBaller baller = new JeskaiBaller();
        harness.setHand(player1, List.of(baller));
        addBallerMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return baller;
    }

    private void addBallerMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Permanent> athleteTokens() {
        return findPermanents(player1, "Athlete");
    }
}
