package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallajiAntiquarian.class, GrizzlyBears.class, Plains.class, SolRing.class, ThayanEvokers.class})
class FallajiAntiquarianTest extends BaseCardTest {

    @Test
    void conjuresCreatureDuplicateWithFixedCostUnearth() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enterAndChoose(target);

        Card duplicate = findDuplicate("Grizzly Bears", target.getOriginalCard().getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, graveyardIndex(duplicate));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(duplicate.getId()));
    }

    @Test
    @DisplayName("A duplicate of a face-down creature has only its face-down values, and the log names no card")
    void duplicateOfAFaceDownCreatureIsANamelessTwoTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        enterAndChoose(target);

        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().satisfies(duplicate -> {
            assertThat(duplicate.getName()).isEmpty();
            assertThat(duplicate.getPower()).isEqualTo(2);
            assertThat(duplicate.getToughness()).isEqualTo(2);
            assertThat(duplicate.isConjured()).isTrue();
        });
        assertThat(gameLogContains("Grizzly Bears")).isFalse();
        assertThat(gameLogContains(player1.getUsername()
                + " conjures a duplicate of a face-down permanent into their graveyard.")).isTrue();
    }

    @Test
    void canTargetAControlledArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        enterAndChoose(target);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sol Ring")
                        && !card.getId().equals(target.getOriginalCard().getId()));
    }

    @Test
    void cannotTargetAControlledLandOrItself() {
        harness.addToBattlefield(player1, new Plains());
        harness.enterBattlefieldAndReturn(player1, new FallajiAntiquarian());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Fallaji Antiquarian"));
    }

    private void enterAndChoose(Permanent target) {
        harness.enterBattlefieldAndReturn(player1, new FallajiAntiquarian());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Card findDuplicate(String name, UUID originalId) {
        return gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals(name) && !card.getId().equals(originalId))
                .findFirst()
                .orElseThrow();
    }

    private int graveyardIndex(Card card) {
        return gd.playerGraveyards.get(player1.getId()).indexOf(card);
    }

    @Test
    void conjuringTheDuplicateIntoTheGraveyardTriggersConjureAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.enterBattlefieldAndReturn(player1, new FallajiAntiquarian());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        findDuplicate("Grizzly Bears", target.getOriginalCard().getId());
        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
