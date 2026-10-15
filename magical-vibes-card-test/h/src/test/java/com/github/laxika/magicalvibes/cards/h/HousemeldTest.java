package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Housemeld.class, GrizzlyBears.class, Forest.class, Quicken.class})
class HousemeldTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature, perpetually makes it an enchantment, then returns it under the caster's control")
    void exilesAndReturnsCreatureAsEnchantmentUnderCasterControl() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castHousemeld(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(target.getCard().getId()).card().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(gd.findExiledCard(target.getCard().getId()).card().hasType(CardType.CREATURE)).isFalse();

        advanceToNextEndStep();

        Permanent returned = findPermanents(player1, "Grizzly Bears").stream().findFirst().orElseThrow();
        assertThat(returned.getCard().getId()).isEqualTo(target.getCard().getId());
        assertThat(returned.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(returned.getCard().hasType(CardType.CREATURE)).isFalse();
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Housemeld()));
        addHousemeldMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Becoming only an enchantment removes creature subtypes in exile")
    void removesCreatureSubtypes() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castHousemeld(target);

        assertThat(harness.getGameQueryService().cardHasSubtype(
                gd.findExiledCard(target.getCard().getId()).card(), CardSubtype.BEAR, gd, player2.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Housemeld cast during an opponent's turn waits for the caster's end step")
    void waitsForCastersEndStep() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Quicken()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);
        castHousemeld(target);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.setLibrary(player1, List.of(new Forest()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castHousemeld(Permanent target) {
        harness.setHand(player1, List.of(new Housemeld()));
        addHousemeldMana(player1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addHousemeldMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void advanceToNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
