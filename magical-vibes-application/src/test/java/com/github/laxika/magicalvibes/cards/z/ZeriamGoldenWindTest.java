package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZeriamGoldenWind.class, EkunduGriffin.class, GrizzlyBears.class})
class ZeriamGoldenWindTest extends BaseCardTest {

    @Test
    @DisplayName("A Griffin dealing combat damage creates a 2/2 white Griffin token with flying")
    void griffinCombatDamageCreatesToken() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new EkunduGriffin());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Griffin");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GRIFFIN);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each Griffin that deals combat damage creates its own token")
    void eachGriffinCreatesToken() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new EkunduGriffin());
        addCreatureReady(player1, new EkunduGriffin());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Griffin")).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Griffin dealing combat damage does not trigger Zeriam")
    void nonGriffinDoesNotTrigger() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Griffin")).isZero();
    }

    @Test
    @DisplayName("Zeriam's own combat damage creates exactly one token, regardless of damage amount")
    void zeriamTriggersForItsOwnCombatDamage() {
        addCreatureReady(player1, new ZeriamGoldenWind());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Griffin")).isEqualTo(1);
        assertThat(countPermanents(player2, "Griffin")).isZero();
        Permanent token = findPermanent(player1, "Griffin");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("An opposing Griffin dealing combat damage does not trigger Zeriam")
    void opposingGriffinDoesNotTrigger() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player2, new EkunduGriffin());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Griffin")).isZero();
        assertThat(countPermanents(player2, "Griffin")).isZero();
    }

    @Test
    @DisplayName("A blocked Griffin dealing damage only to a creature creates no token")
    void combatDamageToCreatureDoesNotTrigger() {
        addCreatureReady(player1, new ZeriamGoldenWind());
        addCreatureReady(player1, new EkunduGriffin());
        addCreatureReady(player2, new EkunduGriffin());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ekundu Griffin");
        harness.assertInGraveyard(player2, "Ekundu Griffin");
        assertThat(countPermanents(player1, "Griffin")).isZero();
    }

    @Test
    @DisplayName("A Griffin token can trigger Zeriam on a later turn")
    void createdTokenCanCreateAnotherToken() {
        addCreatureReady(player1, new ZeriamGoldenWind());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Griffin")).isEqualTo(1);

        harness.performUntapStep(player1);
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(countPermanents(player1, "Griffin")).isEqualTo(2);
    }
}
