package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RiteOfBelzenlok;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheManyDeedsOfBelzenlok.class, RiteOfBelzenlok.class})
class TheManyDeedsOfBelzenlokTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I can exile a Saga from any graveyard and copy its chapter I ability")
    void chapterICopiesSagaChapterFromOpponentGraveyard() {
        RiteOfBelzenlok rite = new RiteOfBelzenlok();
        harness.setGraveyard(player2, List.of(rite));
        harness.setHand(player1, List.of(new TheManyDeedsOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(rite.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("chapter I"));

        harness.passBothPriorities();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(rite);

        harness.passBothPriorities();
        long clericCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Cleric") && permanent.getCard().isToken())
                .count();
        assertThat(clericCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter I does nothing when no Saga is available in a graveyard")
    void chapterISkipsWithoutSagaTarget() {
        harness.setHand(player1, List.of(new TheManyDeedsOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .extracting("name").containsExactly("The Many Deeds of Belzenlok");
    }
}
