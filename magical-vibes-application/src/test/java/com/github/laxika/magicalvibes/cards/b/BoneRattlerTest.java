package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BoneRattler.class)
@DisplayName("Bone Rattler")
class BoneRattlerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it exiles itself and creates four Reassembling Skeleton token cards")
    void deathCreatesReassemblingSkeletonTokenCards() {
        resolveDeathTrigger();

        harness.assertNotInGraveyard(player1, "Bone Rattler");
        List<Card> skeletonCards = graveyardCardsNamed("Reassembling Skeleton");
        assertThat(skeletonCards).hasSize(4);
        assertThat(skeletonCards).allSatisfy(card -> {
            assertThat(card.isToken()).isTrue();
            assertThat(card.isTokenCard()).isTrue();
            assertThat(card.getManaCost()).isEqualTo("{1}{B}");
            assertThat(card.getGraveyardActivatedAbilities()).hasSize(1);
        });
    }

    @Test
    @DisplayName("A Reassembling Skeleton token card can return itself from the graveyard")
    void tokenCardReturnsToBattlefieldTapped() {
        resolveDeathTrigger();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int skeletonIndex = firstGraveyardIndexNamed("Reassembling Skeleton");
        harness.activateGraveyardAbility(player1, skeletonIndex);
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Reassembling Skeleton");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).hasSize(3);
    }

    private void resolveDeathTrigger() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent rattler = harness.addToBattlefieldAndReturn(player1, new BoneRattler());
        rattler.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Bone Rattler");
        resolveAllTriggers();
    }

    private List<Card> graveyardCardsNamed(String name) {
        return gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals(name))
                .toList();
    }

    private int firstGraveyardIndexNamed(String name) {
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("No graveyard card named " + name);
    }
}
