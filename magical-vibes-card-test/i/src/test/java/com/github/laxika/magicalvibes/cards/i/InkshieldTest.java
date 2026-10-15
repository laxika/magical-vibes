package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Inkshield.class, LowlandGiant.class, Fog.class, LightningBolt.class})
class InkshieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage to you and creates one flying Inkling per damage prevented")
    void preventsCombatDamageAndCreatesInklings() {
        addCreatureReady(player2, new LowlandGiant());
        castOnOpponentsTurn();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        List<Permanent> inklings = findPermanents(player1, "Inkling");
        assertThat(inklings).hasSize(4);
        for (Permanent inkling : inklings) {
            assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
            assertThat(inkling.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
            assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    void createsTokensForAllUnblockedAttackers() {
        addCreatureReady(player2, new LowlandGiant());
        addCreatureReady(player2, new LowlandGiant());
        castOnOpponentsTurn();

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Inkling")).hasSize(8);
    }

    @Test
    void doesNotPreventCombatDamageToCreatures() {
        addCreatureReady(player2, new LowlandGiant());
        addCreatureReady(player1, new LowlandGiant());
        castOnOpponentsTurn();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Lowland Giant");
        harness.assertInGraveyard(player2, "Lowland Giant");
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }

    @Test
    void competingFogRequiresDefendersPreventionChoice() {
        addCreatureReady(player2, new LowlandGiant());
        castOnOpponentsTurn();
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void doesNotPreventNoncombatDamage() {
        castOnOpponentsTurn();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }

    @Test
    void preventionExpiresAfterTheTurn() {
        castOnOpponentsTurn();
        harness.passUntilWithNoAttackers(player2, com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passUntil(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);
        addCreatureReady(player2, new LowlandGiant());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 16);
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }

    private void castOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Inkshield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
