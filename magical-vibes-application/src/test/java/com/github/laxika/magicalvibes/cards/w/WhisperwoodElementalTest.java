package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperwoodElemental.class, GrizzlyBears.class, WrathOfGod.class})
class WhisperwoodElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card at the beginning of its controller's end step")
    void manifestsAtEndStep() {
        harness.addToBattlefield(player1, new WhisperwoodElemental());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Sacrifice ability grants death manifest only to face-up nontoken creatures")
    void sacrificeAbilityFiltersGrantedDeathTrigger() {
        Permanent whisperwood = addCreatureReady(player1, new WhisperwoodElemental());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent faceDownCreature = addCreatureReady(player1, new GrizzlyBears());
        faceDownCreature.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, tokenCreature());
        addCreatureReady(player2, new GrizzlyBears());

        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Card thirdCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        Card opponentTopCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentTopCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(manifested -> assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(whisperwood.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, thirdCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
    }

    @Test
    void doesNotManifestAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new WhisperwoodElemental());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
    }

    @Test
    void emptyLibraryDoesNotCauseLossWhenManifesting() {
        harness.addToBattlefield(player1, new WhisperwoodElemental());
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainDeathTrigger() {
        harness.addToBattlefield(player1, new WhisperwoodElemental());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void creaturesEnteringBeforeResolutionGainDeathTrigger() {
        harness.addToBattlefield(player1, new WhisperwoodElemental());
        harness.activateAbility(player1, 0, null, null);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Card topCard = new WrathOfGod();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.isManifested()).isTrue();
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.getCard().getId()).isEqualTo(topCard.getId());
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Card tokenCreature() {
        Card token = new GrizzlyBears();
        token.setToken(true);
        return token;
    }
}
