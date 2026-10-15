package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.m.MyrSuperion;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrapyardSalvo.class, PristineTalisman.class, GlistenerElf.class, MyrSuperion.class, KarnLiberated.class})
class ScrapyardSalvoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to target player equal to artifact cards in controller's graveyard")
    void dealsDamageEqualToArtifactCardsInGraveyard() {
        // Put 3 artifact cards in player1's graveyard
        Card artifact1 = new PristineTalisman();
        Card artifact2 = new PristineTalisman();
        Card artifact3 = new PristineTalisman();
        harness.setGraveyard(player1, List.of(artifact1, artifact2, artifact3));

        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17); // 20 - 3
    }

    @Test
    @DisplayName("Deals zero damage when no artifact cards in graveyard")
    void dealsZeroDamageWithNoArtifacts() {
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count non-artifact cards in graveyard")
    void doesNotCountNonArtifactCards() {
        // Put a creature (non-artifact) and an artifact in the graveyard
        Card creature = new GlistenerElf();
        Card artifact = new PristineTalisman();
        harness.setGraveyard(player1, List.of(creature, artifact));

        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only 1 artifact card, so 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // 20 - 1
    }

    @Test
    @DisplayName("Counts artifact creature cards in graveyard")
    void countsArtifactCreatureCards() {
        // Put an artifact creature in the graveyard (has ARTIFACT as additional type)
        Card artifactCreature = new MyrSuperion();
        Card artifact = new PristineTalisman();
        harness.setGraveyard(player1, List.of(artifactCreature, artifact));

        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // 2 artifact cards (artifact creature + pure artifact)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // 20 - 2
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Scrapyard Salvo");
    }

    @Test
    @DisplayName("Can damage a planeswalker without damaging its controller")
    void damagesPlaneswalker() {
        Permanent karn = harness.addToBattlefieldAndReturn(player2, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        harness.setGraveyard(player1, List.of(new PristineTalisman(), new MyrSuperion()));
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, karn.getId());

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only counts controller's graveyard, not opponent's artifacts or battlefield artifacts")
    void ignoresArtifactsOutsideControllersGraveyard() {
        harness.setGraveyard(player1, List.of(new PristineTalisman()));
        harness.setGraveyard(player2, List.of(new PristineTalisman(), new MyrSuperion()));
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Counts artifacts at resolution rather than when cast")
    void countsGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of(new PristineTalisman()));
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.setGraveyard(player1, List.of(new PristineTalisman(), new MyrSuperion()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        harness.setGraveyard(player1, List.of(new PristineTalisman()));
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
