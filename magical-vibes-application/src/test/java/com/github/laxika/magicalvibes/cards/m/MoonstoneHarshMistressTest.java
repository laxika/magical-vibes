package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonstoneHarshMistress.class, Forest.class, GrizzlyBears.class, Sift.class})
class MoonstoneHarshMistressTest extends BaseCardTest {

    @Test
    void acceptingDiscardTriggerExilesCardAndGrantsPlayPermission() {
        Forest discarded = discardCard();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
        assertThat(gd.exilePlayPermissions).containsEntry(discarded.getId(), player1.getId());

        harness.castFromExile(player1, discarded.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(discarded.getId()));
    }

    @Test
    void decliningDiscardTriggerLeavesCardInGraveyard() {
        Card discarded = discardCard();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    private Forest discardCard() {
        harness.addToBattlefield(player1, new MoonstoneHarshMistress());
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(new Sift(), discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        return discarded;
    }
}
