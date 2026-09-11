package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FLiThePathfinder.class, FountainOfYouth.class, GrizzlyBears.class})
class FLiThePathfinderTest extends BaseCardTest {

    @Test
    void boostsAllCreaturesAfterEnduringStory() {
        Permanent fili = harness.addToBattlefieldAndReturn(player1, new FLiThePathfinder());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    void ownEntryCreatesDwarfToken() {
        harness.enterBattlefieldAndReturn(player1, new FLiThePathfinder());
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DWARF);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void nontokenDwarfEntryCreatesDwarfToken() {
        harness.addToBattlefield(player1, new FLiThePathfinder());
        harness.enterBattlefieldAndReturn(player1, dwarfCard(false));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void nonDwarfAndDwarfTokenEntriesDoNotTrigger() {
        harness.addToBattlefield(player1, new FLiThePathfinder());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, dwarfCard(true));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Dwarf"))
                .isEmpty();
    }

    private static Card dwarfCard(boolean token) {
        Card card = new Card();
        card.setName(token ? "Dwarf Token" : "Dwarf");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.DWARF));
        card.setPower(2);
        card.setToughness(2);
        card.setToken(token);
        return card;
    }
}
