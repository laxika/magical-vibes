package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spellshift.class, Divination.class, Forest.class, LightningBolt.class})
class SpellshiftTest extends BaseCardTest {

    @Test
    void decliningTheFreeCastCountersTheTargetAndShufflesTheRevealedCards() {
        LightningBolt target = new LightningBolt();
        Forest land = new Forest();
        Divination found = new Divination();
        castSpellshift(target, List.of(land, found));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(target.getId()));
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Divination");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, found);
    }

    @Test
    void acceptingTheFreeCastCastsTheFoundSpellAndShufflesTheOtherRevealedCards() {
        LightningBolt target = new LightningBolt();
        Forest land = new Forest();
        Divination found = new Divination();
        castSpellshift(target, List.of(land, found));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(found);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void noInstantOrSorceryIsFoundAndTheTargetIsStillCountered() {
        LightningBolt target = new LightningBolt();
        Forest land = new Forest();
        castSpellshift(target, List.of(land));

        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    private void castSpellshift(LightningBolt target, List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, library);

        harness.setHand(player2, List.of(new Spellshift()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
