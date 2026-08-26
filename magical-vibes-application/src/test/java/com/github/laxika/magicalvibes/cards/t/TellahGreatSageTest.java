package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TellahGreatSage.class, Concentrate.class, GrizzlyBears.class, MindSpring.class, Spellbook.class})
class TellahGreatSageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a Hero token")
    void noncreatureSpellCreatesHero() {
        castTellah();

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        assertThat(findPermanent(player1, "Tellah, Great Sage")).isNotNull();
    }

    @Test
    @DisplayName("Casting a four-mana spell creates a Hero and draws two cards")
    void fourManaSpellDrawsTwoCards() {
        castTellah();

        harness.setHand(player1, List.of(new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 5);
        assertThat(findPermanent(player1, "Tellah, Great Sage")).isNotNull();
    }

    @Test
    @DisplayName("Casting a spell with eight mana spent sacrifices Tellah and damages each opponent")
    void eightManaSpellSacrificesTellahAndDealsSpentManaDamage() {
        castTellah();

        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castSorcery(player1, 0, 6);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Tellah, Great Sage");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 8);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Tellah")
    void creatureSpellDoesNotTrigger() {
        castTellah();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isZero();
        assertThat(findPermanent(player1, "Tellah, Great Sage")).isNotNull();
    }

    private void castTellah() {
        harness.setHand(player1, List.of(new TellahGreatSage()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
