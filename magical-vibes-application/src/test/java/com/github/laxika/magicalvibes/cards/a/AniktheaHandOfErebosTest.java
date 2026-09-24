package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornMarauder;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AniktheaHandOfErebos.class, NyxbornMarauder.class, SongOfTheDryads.class, GrizzlyBears.class})
class AniktheaHandOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other enchantment creatures you control menace")
    void givesEnchantmentCreaturesMenace() {
        Permanent anikthea = harness.addToBattlefieldAndReturn(player1, new AniktheaHandOfErebos());
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornMarauder());
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentEnchantmentCreature = harness.addToBattlefieldAndReturn(player2, new NyxbornMarauder());

        assertThat(gqs.hasKeyword(gd, anikthea, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantmentCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ordinaryCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEnchantmentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Enters and creates a black 3/3 Zombie copy of a non-Aura enchantment")
    void entersAndCreatesNonAuraEnchantmentCopy() {
        NyxbornMarauder marauder = new NyxbornMarauder();
        SongOfTheDryads aura = new SongOfTheDryads();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(marauder, aura, bears));
        harness.enterBattlefieldAndReturn(player1, new AniktheaHandOfErebos());

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(marauder.getId());

        harness.handleMultipleCardsChosen(player1, List.of(marauder.getId()));
        harness.passBothPriorities();

        Permanent token = tokenNamed("Nyxborn Marauder");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getSubtypes()).containsAll(marauder.getSubtypes());
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Song of the Dryads");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking creates the same optional copy trigger")
    void attackingCreatesCopyTrigger() {
        addCreatureReady(player1, new AniktheaHandOfErebos());

        NyxbornMarauder marauder = new NyxbornMarauder();
        harness.setGraveyard(player1, List.of(marauder));
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(marauder.getId());
        harness.handleMultipleCardsChosen(player1, List.of(marauder.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Nyxborn Marauder"));
    }

    private Permanent tokenNamed(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
