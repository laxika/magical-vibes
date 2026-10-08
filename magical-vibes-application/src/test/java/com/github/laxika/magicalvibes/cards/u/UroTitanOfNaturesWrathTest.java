package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UroTitanOfNaturesWrath.class, Forest.class, NyxbornColossus.class})
class UroTitanOfNaturesWrathTest extends BaseCardTest {

    @Test
    void normalCastSacrificesUroAndResolvesItsTrigger() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.setHand(player1, List.of(new UroTitanOfNaturesWrath(), forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof UroTitanOfNaturesWrath);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void escapedCastKeepsUroOnTheBattlefield() {
        UroTitanOfNaturesWrath uro = new UroTitanOfNaturesWrath();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(uro);
        graveyard.addAll(IntStream.range(0, 5).mapToObj(ignored -> new NyxbornColossus()).toList());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromGraveyard(player1, 0, IntStream.rangeClosed(1, 5).boxed().toList());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent escapedUro = findPermanent(player1, "Uro, Titan of Nature's Wrath");
        assertThat(escapedUro).isNotNull();
        assertThat(escapedUro.isEscaped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void attackingUroResolvesItsTrigger() {
        addCreatureReady(player1, new UroTitanOfNaturesWrath());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.setHand(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void enteringUroPutsTwoSeparateTriggeredAbilitiesOnTheStack() {
        harness.setHand(player1, List.of(new UroTitanOfNaturesWrath()));
        harness.setLibrary(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Uro, Titan of Nature's Wrath")).isNotNull();
    }

    @Test
    void mayDeclineLandWithoutLosingLifeGainOrDraw() {
        addCreatureReady(player1, new UroTitanOfNaturesWrath());
        NyxbornColossus drawn = new NyxbornColossus();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, drawn);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    void mayPutJustDrawnLandOntoBattlefieldAfterPlayingLandForTurn() {
        addCreatureReady(player1, new UroTitanOfNaturesWrath());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(drawn);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotCountUroAmongFiveCardsExiledForEscape() {
        harness.setGraveyard(player1, List.of(new UroTitanOfNaturesWrath(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
