package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Housemeld.class, GrizzlyBears.class, Forest.class})
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

    private void castHousemeld(Permanent target) {
        harness.setHand(player1, List.of(new Housemeld()));
        addHousemeldMana(player1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addHousemeldMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void advanceToNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
