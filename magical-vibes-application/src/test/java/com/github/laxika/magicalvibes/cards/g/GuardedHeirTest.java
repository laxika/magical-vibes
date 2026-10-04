package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardedHeir.class, BurstLightning.class})
class GuardedHeirTest extends BaseCardTest {

    @Test
    @DisplayName("When Guarded Heir enters the battlefield, two Knight tokens are created")
    void etbCreatesTwoKnightTokens() {
        harness.setHand(player1, List.of(new GuardedHeir()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
    }

    @Test
    @DisplayName("Guarded Heir's ETB tokens are 3/3 white Knights")
    void etbTokensHaveCorrectProperties() {
        harness.setHand(player1, List.of(new GuardedHeir()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Knight");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
            assertThat(token.getCard().isToken()).isTrue();
        });
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GuardedHeir());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void tokensDoNotInheritLifelink() {
        harness.setHand(player1, List.of(new GuardedHeir()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        findPermanents(player1, "Knight").forEach(token -> token.setSummoningSick(false));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1, 2));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void triggerCreatesTokensAfterHeirDiesInResponse() {
        harness.setHand(player1, List.of(new GuardedHeir()));
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).isEmpty();
        Permanent heir = findPermanent(player1, "Guarded Heir");
        harness.castAndResolveInstant(player2, 0, heir.getId());
        assertThat(findPermanents(player1, "Guarded Heir")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        assertThat(findPermanents(player2, "Knight")).isEmpty();
    }
}
