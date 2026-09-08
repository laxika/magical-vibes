package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StanggTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates the linked Stangg Twin token")
    void entersCreatesTwin() {
        castStangg();

        Permanent twin = findTwin();
        assertThat(twin.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(twin.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(twin.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(twin.getEffectivePower()).isEqualTo(3);
        assertThat(twin.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("When Stangg leaves the battlefield, its Twin is exiled")
    void stanggLeavingExilesTwin() {
        castStangg();
        Permanent stangg = findStangg();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, stangg));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findTwinOrNull()).isNull();
        assertThat(findStanggOrNull()).isNull();
    }

    @Test
    @DisplayName("When the Twin leaves the battlefield, Stangg is sacrificed")
    void twinLeavingSacrificesStangg() {
        castStangg();
        Permanent twin = findTwin();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, twin));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findStanggOrNull()).isNull();
        harness.assertInGraveyard(player1, "Stangg");
    }

    @Test
    @DisplayName("If Stangg leaves before its ETB ability resolves, the Twin remains")
    void twinRemainsWhenStanggLeavesBeforeEtbResolves() {
        harness.setHand(player1, List.of(new Stangg()));
        addStanggMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stangg = findStangg();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, stangg));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findTwinOrNull()).isNotNull();
    }

    private void castStangg() {
        harness.setHand(player1, List.of(new Stangg()));
        addStanggMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addStanggMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent findStangg() {
        return findStanggOrNull();
    }

    private Permanent findStanggOrNull() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stangg"))
                .findFirst()
                .orElse(null);
    }

    private Permanent findTwin() {
        return findTwinOrNull();
    }

    private Permanent findTwinOrNull() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stangg Twin"))
                .findFirst()
                .orElse(null);
    }
}
