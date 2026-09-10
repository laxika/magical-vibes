package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmeritusOfIdeationAncestralRecallTest extends BaseCardTest {

    @Test
    @DisplayName("Enters prepared with an Ancestral Recall copy")
    void entersPrepared() {
        Permanent emeritus = castEmeritus();

        assertThat(emeritus.isPrepared()).isTrue();
        UUID copyId = emeritus.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @DisplayName("Casting Ancestral Recall draws three cards and unprepares Emeritus")
    void castingPreparedCopyDrawsAndUnprepares() {
        Permanent emeritus = castEmeritus();
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, player2.getId());
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking can prepare Emeritus again only after exiling eight cards")
    void attackingWithEightGraveyardCardsPreparesAgain() {
        Permanent emeritus = castEmeritus();
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, player2.getId());
        harness.passBothPriorities();

        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        emeritus.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(emeritus)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> !card.getId().equals(emeritus.getPreparedSpellCardId()))
                .containsExactlyElementsOf(graveyard);
    }

    @Test
    @DisplayName("Attacking with fewer than eight graveyard cards does not prepare Emeritus")
    void attackingWithFewerThanEightGraveyardCardsDoesNotPrepare() {
        Permanent emeritus = castEmeritus();
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, player2.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        emeritus.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(emeritus)));
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    private Permanent castEmeritus() {
        harness.setHand(player1, List.of(new EmeritusOfIdeationAncestralRecall()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Emeritus of Ideation");
    }
}
