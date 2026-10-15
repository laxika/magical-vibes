package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RukarumelBiologist.class})
class RukarumelBiologistTest extends BaseCardTest {

    @Test
    @DisplayName("Rukarumel grants the chosen type to nontoken creatures and Slivers")
    void grantsChosenTypeToNontokensAndSlivers() {
        Permanent nontokenCreature = addCreatureReady(player1,
                creature("Bear", CardSubtype.BEAR, false));
        Permanent sliverToken = addCreatureReady(player1,
                creature("Sliver Token", CardSubtype.SLIVER, true));
        Permanent otherToken = addCreatureReady(player1,
                creature("Bear Token", CardSubtype.BEAR, true));
        Permanent opponentCreature = addCreatureReady(player2,
                creature("Opponent Bear", CardSubtype.BEAR, false));

        harness.castFromHand(player1, new RukarumelBiologist(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.computeStaticBonus(gd, nontokenCreature).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, sliverToken).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, otherToken).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);

        Card creatureInHand = creature("Hand Bear", CardSubtype.BEAR, false);
        gd.playerHands.get(player1.getId()).add(creatureInHand);
        assertThat(gqs.getCardSubtypes(creatureInHand, gd, player1.getId()))
                .contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Rukarumel creates a 1/1 colorless Sliver token")
    void createsSliverToken() {
        addCreatureReady(player1, new RukarumelBiologist());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Sliver");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SLIVER);
    }

    @Test
    void grantsChosenTypeToItselfAndNewSliverTokensWithoutReplacingTheirTypes() {
        harness.castFromHand(player1, new RukarumelBiologist(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(gqs.effectiveCreatureSubtypes(gd, source))
                .contains(CardSubtype.HUMAN, CardSubtype.WIZARD, CardSubtype.GOBLIN);
        source.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, token))
                .contains(CardSubtype.SLIVER, CardSubtype.GOBLIN);
    }

    @Test
    void grantsTypeInAllOwnedNonBattlefieldZonesAndStopsWhenSourceLeaves() {
        harness.castFromHand(player1, new RukarumelBiologist(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        Card handCard = new RukarumelBiologist();
        Card libraryCard = new RukarumelBiologist();
        Card graveyardCard = new RukarumelBiologist();
        Card exileCard = new RukarumelBiologist();
        Card opponentCard = new RukarumelBiologist();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exileCard));
        harness.setHand(player2, List.of(opponentCard));

        for (Card card : List.of(handCard, libraryCard, graveyardCard, exileCard)) {
            assertThat(gqs.getCardSubtypes(card, gd, player1.getId()))
                    .contains(CardSubtype.HUMAN, CardSubtype.WIZARD, CardSubtype.GOBLIN);
        }
        assertThat(gqs.getCardSubtypes(opponentCard, gd, player2.getId()))
                .doesNotContain(CardSubtype.GOBLIN);

        gd.playerBattlefields.get(player1.getId()).clear();
        for (Card card : List.of(handCard, libraryCard, graveyardCard, exileCard)) {
            assertThat(gqs.getCardSubtypes(card, gd, player1.getId()))
                    .contains(CardSubtype.HUMAN, CardSubtype.WIZARD)
                    .doesNotContain(CardSubtype.GOBLIN);
        }
    }

    @Test
    void grantsChosenTypeToCreatureSpellsOnTheStack() {
        harness.castFromHand(player1, new RukarumelBiologist(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        Card spell = new RukarumelBiologist();
        harness.castFromHand(player1, spell, "{W}{U}{B}{R}{G}");

        assertThat(gqs.getCardSubtypes(gd.stack.getFirst().getCard(), gd, player1.getId()))
                .contains(CardSubtype.HUMAN, CardSubtype.WIZARD, CardSubtype.GOBLIN);
    }

    @Test
    @CardUsed({Bitterblossom.class, ArtificialEvolution.class})
    void grantsChosenTypeToNoncreatureSliverPermanents() {
        harness.castFromHand(player1, new RukarumelBiologist(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");

        assertThat(gqs.hasEffectiveSubtype(gd, enchantment, CardSubtype.SLIVER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, enchantment, CardSubtype.GOBLIN)).isTrue();
    }

    private static Card creature(String name, CardSubtype subtype, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        card.setToken(token);
        return card;
    }
}
