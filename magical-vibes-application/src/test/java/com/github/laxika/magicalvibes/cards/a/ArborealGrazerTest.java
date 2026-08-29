package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArborealGrazer.class, Forest.class})
class ArborealGrazerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a land from hand onto the battlefield tapped")
    void etbPutsLandOntoBattlefieldTapped() {
        Forest forest = new Forest();
        castArborealGrazer(forest);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent permanent = findPermanent(forest);
        assertThat(permanent).isNotNull();
        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB leaves the land in hand")
    void decliningLeavesLandInHand() {
        Forest forest = new Forest();
        castArborealGrazer(forest);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        assertThat(findPermanent(forest)).isNull();
    }

    private void castArborealGrazer(Forest forest) {
        harness.setHand(player1, List.of(new ArborealGrazer(), forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
