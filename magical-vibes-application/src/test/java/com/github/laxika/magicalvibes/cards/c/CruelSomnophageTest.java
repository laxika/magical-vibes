package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruelSomnophage.class, CantWakeUp.class, GrizzlyBears.class, Forest.class})
class CruelSomnophageTest extends BaseCardTest {

    @Test
    void adventureMillsFourCardsAndExilesTheCard() {
        List<Card> milled = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, milled);
        CruelSomnophage card = new CruelSomnophage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCountsCreatureCardsInAllGraveyards() {
        Permanent somnophage = addCruelSomnophageReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, somnophage)).isEqualTo(5);
    }

    @Test
    void creatureFaceUpdatesAsCreatureCardsEnterAndLeaveGraveyards() {
        Permanent somnophage = addCruelSomnophageReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());

        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(1);

        graveyard.add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(2);

        graveyard.removeFirst();
        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(1);
    }

    private Permanent addCruelSomnophageReady(Player player) {
        Permanent permanent = new Permanent(new CruelSomnophage());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
