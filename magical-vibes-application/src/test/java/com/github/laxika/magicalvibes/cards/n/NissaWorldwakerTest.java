package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NissaWorldwakerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 turns a land you control into a 4/4 Elemental with trample that is still a land")
    void plusOneAnimatesOwnLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1, new Forest());

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(4);
        assertThat(forest.getEffectiveToughness()).isEqualTo(4);
        assertThat(forest.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("+1 animation is permanent and survives the end of the turn")
    void plusOneAnimationSurvivesEndOfTurn() {
        addReadyNissa(player1, 3);
        Permanent forest = addLand(player1, new Forest());

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 cannot animate a land an opponent controls")
    void plusOneCannotTargetOpponentLand() {
        addReadyNissa(player1, 3);
        Permanent oppForest = addLand(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, oppForest.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second +1 untaps up to four target Forests, including an opponent's")
    void plusOneUntapsForests() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest1 = addLand(player1, new Forest());
        Permanent forest2 = addLand(player1, new Forest());
        Permanent oppForest = addLand(player2, new Forest());
        forest1.tap();
        forest2.tap();
        oppForest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(forest1.getId(), forest2.getId(), oppForest.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
        assertThat(oppForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second +1 cannot target a non-Forest land")
    void plusOneUntapRejectsNonForest() {
        addReadyNissa(player1, 3);
        Permanent mountain = addLand(player1, new Mountain());
        mountain.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−7 offers only basic land cards from the library")
    void ultimateOffersOnlyBasicLands() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(2)
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("−7 puts the found basic lands onto the battlefield as 4/4 Elementals with trample")
    void ultimatePutsLandsOntoBattlefieldAnimated() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        List<Permanent> newLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(newLands).hasSize(2);
        assertThat(newLands).allSatisfy(land -> {
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(land.getEffectivePower()).isEqualTo(4);
            assertThat(land.getEffectiveToughness()).isEqualTo(4);
            assertThat(land.hasKeyword(Keyword.TRAMPLE)).isTrue();
            assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
            assertThat(land.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("−7 stopping the search early animates only the lands actually found")
    void ultimateStoppingEarlyAnimatesOnlyFoundLands() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        List<Permanent> newLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(newLands).hasSize(1);
        assertThat(gqs.isCreature(gd, newLands.getFirst())).isTrue();
        assertThat(newLands.getFirst().getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("−7 cannot be activated with insufficient loyalty")
    void ultimateNeedsSevenLoyalty() {
        addReadyNissa(player1, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = new Permanent(new NissaWorldwaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addLand(Player player, Card land) {
        Permanent perm = new Permanent(land);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Plains(), new GrizzlyBears()));
    }
}
