package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.m.MoldervineReclamation;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelloBardOfTheBrambles.class, ThranDynamo.class, MoldervineReclamation.class,
        AssaultSuit.class, SolRing.class})
class BelloBardOfTheBramblesTest extends BaseCardTest {

    @Test
    @DisplayName("Animates qualifying artifacts and enchantments only during your turn")
    void animatesQualifyingPermanentsDuringControllerTurn() {
        harness.addToBattlefield(player1, new BelloBardOfTheBrambles());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranDynamo());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MoldervineReclamation());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        Permanent cheapArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new ThranDynamo());

        harness.forceActivePlayer(player1);

        assertAnimated(artifact);
        assertAnimated(enchantment);
        assertThat(gqs.isCreature(gd, equipment)).isFalse();
        assertThat(gqs.isCreature(gd, cheapArtifact)).isFalse();
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();

        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.isCreature(gd, enchantment)).isFalse();
    }

    @Test
    @DisplayName("Animated permanents draw a card when they deal combat damage to a player")
    void animatedPermanentDrawsOnCombatDamage() {
        harness.addToBattlefield(player1, new BelloBardOfTheBrambles());
        Permanent dynamo = addReadyPermanent(player1, new ThranDynamo());
        dynamo.setAttacking(true);
        harness.setLibrary(player1, List.of(new BelloBardOfTheBrambles()));
        harness.forceActivePlayer(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void assertAnimated(Permanent permanent) {
        assertThat(gqs.isCreature(gd, permanent)).isTrue();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, permanent)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
