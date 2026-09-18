package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.FuneralPyre;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KeepWatch;
import com.github.laxika.magicalvibes.cards.w.WebOfInertia;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullmageAdvocate.class, Bonesplitter.class, FuneralPyre.class, KeepWatch.class,
        WebOfInertia.class, GiantWarthog.class})
class NullmageAdvocateTest extends BaseCardTest {

    @Test
    void returnsTwoCardsFromOpponentsGraveyardAndDestroysArtifact() {
        Permanent advocate = addReadyAdvocate();
        Card first = new FuneralPyre();
        Card second = new KeepWatch();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(artifact.getCard().getId()));
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    void returnsTwoCardsFromOpponentsGraveyardAndDestroysEnchantment() {
        Permanent advocate = addReadyAdvocate();
        Card first = new FuneralPyre();
        Card second = new KeepWatch();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new WebOfInertia());
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(enchantment.getCard().getId()));
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    void rejectsCreatureAsTheDestructionTarget() {
        Permanent advocate = addReadyAdvocate();
        Card first = new FuneralPyre();
        Card second = new KeepWatch();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setGraveyard(player2, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCardsFromControllerGraveyard() {
        Permanent advocate = addReadyAdvocate();
        Card first = new FuneralPyre();
        Card second = new KeepWatch();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new WebOfInertia());
        harness.setGraveyard(player1, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAdvocate() {
        return addCreatureReady(player1, new NullmageAdvocate());
    }

    private int index(Permanent advocate) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(advocate);
    }
}
