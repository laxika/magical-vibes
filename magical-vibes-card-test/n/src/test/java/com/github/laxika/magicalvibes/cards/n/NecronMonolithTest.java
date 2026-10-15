package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CanoptekWraith;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecronMonolith.class, CanoptekWraith.class, Forest.class, LeylineOfTheVoid.class})
class NecronMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, it mills three and creates a Necron Warrior for each creature milled")
    void attacksMillsAndCreatesOneTokenPerMilledCreature() {
        Card firstCreature = new CanoptekWraith();
        Card nonCreature = new Forest();
        Card secondCreature = new CanoptekWraith();
        harness.setLibrary(player1, List.of(firstCreature, nonCreature, secondCreature));
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstCreature, nonCreature, secondCreature);
        assertThat(findPermanents(player1, "Necron Warrior")).hasSize(2);
        assertThat(findPermanents(player1, "Necron Warrior")).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.NECRON, CardSubtype.WARRIOR);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("It creates no tokens when the milled cards are not creatures")
    void doesNotCreateTokensForNoncreatureCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Necron Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Milled creatures still create tokens when Leyline of the Void exiles them")
    void createsTokensForMilledCreaturesDivertedToExile() {
        Card creature = new CanoptekWraith();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(findPermanents(player1, "Necron Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("A short library mills only its remaining cards and leaves the opponent's library alone")
    void millsRemainingCardsInShortLibrary() {
        Card creature = new CanoptekWraith();
        Card opposingCard = new CanoptekWraith();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(opposingCard));
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingCard);
        assertThat(findPermanents(player1, "Necron Warrior")).hasSize(1);
        assertThat(findPermanents(player2, "Necron Warrior")).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no tokens")
    void emptyLibraryCreatesNoTokens() {
        harness.setLibrary(player1, List.of());
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Necron Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Crew 4 rejects a single creature with two power")
    void cannotCrewWithInsufficientPower() {
        harness.addToBattlefield(player1, new NecronMonolith());
        harness.addToBattlefield(player1, new CanoptekWraith());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Necron Monolith"))).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew and are tapped as the cost")
    void summoningSickCreaturesCanCrew() {
        Permanent monolith = harness.addToBattlefieldAndReturn(player1, new NecronMonolith());
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new CanoptekWraith());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new CanoptekWraith());
        firstCrew.setSummoningSick(true);
        secondCrew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, monolith)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, monolith)).isTrue();
    }

    private void addReadyMonolithAndCrew() {
        addCreatureReady(player1, new NecronMonolith());
        addCreatureReady(player1, new CanoptekWraith());
        addCreatureReady(player1, new CanoptekWraith());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
