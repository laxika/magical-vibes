package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfTheWorldsoul.class, GrizzlyBears.class})
class SongOfTheWorldsoulTest extends BaseCardTest {

    @Test
    void populatesWhenControllerCastsASpell() {
        harness.addToBattlefield(player1, song());
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Soldier Token"));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier Token"))
                .anyMatch(permanent -> !permanent.getId().equals(token.getId()));
    }

    @Test
    void doesNothingWhenControllerHasNoCreatureTokens() {
        harness.addToBattlefield(player1, song());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void doesNotTriggerForAnOpponentsSpell() {
        harness.addToBattlefield(player1, song());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }

    private static SongOfTheWorldsoul song() {
        SongOfTheWorldsoul song = new SongOfTheWorldsoul();
        song.setName("Song of the Worldsoul");
        return song;
    }
}
