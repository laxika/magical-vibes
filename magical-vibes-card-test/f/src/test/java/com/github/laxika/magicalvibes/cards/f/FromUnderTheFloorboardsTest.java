package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromUnderTheFloorboards.class, RavensCrime.class})
class FromUnderTheFloorboardsTest extends BaseCardTest {

    private FromUnderTheFloorboards discardViaRavensCrime() {
        FromUnderTheFloorboards card = new FromUnderTheFloorboards();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return card;
    }

    @Test
    void normalCastCreatesThreeTappedZombiesAndGainsThreeLife() {
        harness.setHand(player1, List.of(new FromUnderTheFloorboards()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(zombies(player1)).hasSize(3).allMatch(Permanent::isTapped);
        assertThat(zombies(player1)).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    void madnessCastUsesXForTokensAndLife() {
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AlternateCastXValueChoice.class);
        harness.handleXValueChosen(player1, 4);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(zombies(player1)).hasSize(4).allMatch(Permanent::isTapped);
    }

    @Test
    void madnessWithZeroXCreatesNoTokensAndGainsNoLife() {
        FromUnderTheFloorboards card = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(zombies(player1)).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void decliningMadnessPutsDiscardedCardIntoGraveyard() {
        FromUnderTheFloorboards card = discardViaRavensCrime();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertLife(player1, 20);
        assertThat(zombies(player1)).isEmpty();
    }

    @Test
    void acceptingMadnessWithoutEnoughManaPutsCardIntoGraveyard() {
        FromUnderTheFloorboards card = discardViaRavensCrime();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertLife(player1, 20);
        assertThat(zombies(player1)).isEmpty();
    }

    private List<Permanent> zombies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
