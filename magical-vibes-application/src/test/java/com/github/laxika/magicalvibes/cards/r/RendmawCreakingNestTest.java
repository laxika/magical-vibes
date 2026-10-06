package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendmawCreakingNest.class, DarksteelMyr.class, DryadArbor.class, GrizzlyBears.class,
        LlanowarWastes.class, TurnToFrog.class})
class RendmawCreakingNestTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by giving each player a tapped, goaded Bird")
    void entersAndCreatesGoadedBirds() {
        castRendmaw();

        assertThat(birds(player1)).hasSize(1);
        assertThat(birds(player2)).hasSize(1);
        assertThat(birds(player1)).allSatisfy(bird -> {
            assertThat(bird.isTapped()).isTrue();
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        });

        Permanent bird = birds(player1).getFirst();
        bird.untap();
        bird.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Triggers when a two-type spell or land is played")
    void triggersForTwoTypeSpellAndLand() {
        addRendmawToBattlefield();

        harness.castFromHand(player1, new DarksteelMyr(), "{3}");
        harness.passBothPriorities();
        assertThat(birds(player1)).hasSize(1);
        assertThat(birds(player2)).hasSize(1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DryadArbor()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(birds(player1)).hasSize(2);
        assertThat(birds(player2)).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for a single-type spell")
    void doesNotTriggerForSingleTypeSpell() {
        addRendmawToBattlefield();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(birds(player1)).isEmpty();
        assertThat(birds(player2)).isEmpty();
    }

    @Test
    @DisplayName("Birds remain goaded after losing all abilities")
    void losingAbilitiesDoesNotRemoveGoad() {
        castRendmaw();
        Permanent bird = birds(player1).getFirst();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, bird.getId());
        bird.untap();
        bird.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("An opponent's Bird must avoid Rendmaw's controller when another player can be attacked")
    void opponentBirdMustAttackAnotherPlayerIfAble() {
        castRendmaw();
        Player thirdPlayer = addOpponent();
        Permanent bird = birds(player2).getFirst();
        bird.untap();
        bird.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack a player other than you");
        assertThat(als.canAttackDefender(gd, bird, thirdPlayer.getId())).isTrue();
    }

    @Test
    @DisplayName("Playing an ordinary land does not create Birds")
    void singleTypeLandDoesNotTrigger() {
        addRendmawToBattlefield();
        harness.setHand(player1, List.of(new LlanowarWastes()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(birds(player1)).isEmpty();
        assertThat(birds(player2)).isEmpty();
    }

    @Test
    @DisplayName("An opponent playing a two-type card does not create Birds")
    void opponentTwoTypeSpellDoesNotTrigger() {
        addRendmawToBattlefield();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new DarksteelMyr(), "{3}");
        resolveAllTriggers();

        assertThat(birds(player1)).isEmpty();
        assertThat(birds(player2)).isEmpty();
    }

    @Test
    @DisplayName("Putting a two-type land onto the battlefield does not count as playing it")
    void enteringWithoutBeingPlayedDoesNotTrigger() {
        addRendmawToBattlefield();
        harness.enterBattlefieldAndReturn(player1, new DryadArbor());
        resolveAllTriggers();

        assertThat(birds(player1)).isEmpty();
        assertThat(birds(player2)).isEmpty();
    }

    @Test
    @DisplayName("Birds remain goaded after Rendmaw leaves and its controller untaps")
    void goadPersistsWithoutRendmawAndAcrossUntap() {
        castRendmaw();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Rendmaw, Creaking Nest"));
        harness.performUntapStep(player1);
        birds(player1).getFirst().setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("An opponent playing a two-type land does not create Birds")
    void opponentTwoTypeLandDoesNotTrigger() {
        addRendmawToBattlefield();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DryadArbor()));
        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(birds(player1)).isEmpty();
        assertThat(birds(player2)).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger creates Birds even if Rendmaw leaves before it resolves")
    void enterTriggerResolvesWithoutRendmaw() {
        harness.castFromHand(player1, new RendmawCreakingNest(), "{3}{B}{G}");
        harness.passBothPriorities();
        assertThat(birds(player1)).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Rendmaw, Creaking Nest"));
        resolveAllTriggers();

        assertThat(birds(player1)).hasSize(1);
        assertThat(birds(player2)).hasSize(1);
        assertThat(birds(player1).getFirst().isTapped()).isTrue();
        assertThat(birds(player2).getFirst().isTapped()).isTrue();
    }

    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Third player");
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(opponent.getUsername());
        gd.playerIdToName.put(opponent.getId(), opponent.getUsername());
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }

    private void castRendmaw() {
        harness.castFromHand(player1, new RendmawCreakingNest(), "{3}{B}{G}");
        resolveAllTriggers();
    }

    private void addRendmawToBattlefield() {
        harness.addToBattlefield(player1, new RendmawCreakingNest());
    }

    private List<Permanent> birds(Player player) {
        return findPermanents(player, "Bird").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
