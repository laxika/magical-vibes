package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphantReckoning.class, GrizzlyBears.class, Ornithopter.class,
        GhostlyPrison.class, IchorWellspring.class, JaceBeleren.class, Pacifism.class})
class TriumphantReckoningTest extends BaseCardTest {

    @Test
    void returnsAllOwnArtifactsEnchantmentsAndPlaneswalkersFromGraveyard() {
        Card artifact = new IchorWellspring();
        Card enchantment = new GhostlyPrison();
        Card planeswalker = new JaceBeleren();
        Card creature = new GrizzlyBears();
        Card opponentArtifact = new IchorWellspring();
        Card reckoning = new TriumphantReckoning();
        harness.setGraveyard(player1, List.of(artifact, enchantment, planeswalker, creature));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(reckoning));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artifact, enchantment, planeswalker);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, reckoning);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    void returnsEveryArtifactCreatureIncludingDuplicateNamesWithoutOptionalChoices() {
        Card first = new Ornithopter();
        Card second = new Ornithopter();
        harness.setGraveyard(player1, List.of(first, second));
        castReckoning();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card == first || card == second);
    }

    @Test
    void resolvesWithNoMatchingCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castReckoning();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Triumphant Reckoning");
    }

    @Test
    void returnsAuraAttachedToChosenExistingCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        castReckoning();

        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == aura)
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(chosen.getId()));
        assertThat(first.getAttachedTo()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    private void castReckoning() {
        harness.setHand(player1, List.of(new TriumphantReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
