package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuperiorSpiderMan.class, GrizzlyBears.class})
class SuperiorSpiderManTest extends BaseCardTest {

    @Test
    void copiesCreatureCardFromAnyGraveyardAndExilesItAfterTheReflexiveTriggerResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new SuperiorSpiderMan()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent superior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Superior Spider-Man"))
                .findFirst()
                .orElseThrow();
        assertThat(superior.getCard().getName()).isEqualTo("Superior Spider-Man");
        assertThat(superior.getCard().getPower()).isEqualTo(4);
        assertThat(superior.getCard().getToughness()).isEqualTo(4);
        assertThat(superior.getCard().getSubtypes())
                .contains(CardSubtype.SPIDER, CardSubtype.HUMAN, CardSubtype.HERO);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNull();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }
}
