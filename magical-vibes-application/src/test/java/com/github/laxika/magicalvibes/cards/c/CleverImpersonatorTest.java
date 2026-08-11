package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CleverImpersonatorTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a noncreature artifact")
    void copiesNoncreatureArtifact() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        castCleverImpersonator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        UUID relicId = harness.getPermanentId(player2, "Darksteel Relic");
        harness.handlePermanentChosen(player1, relicId);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clever Impersonator"))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isFalse();
    }

    @Test
    @DisplayName("Cannot copy a land")
    void cannotCopyLand() {
        harness.addToBattlefield(player2, new Island());
        castCleverImpersonator();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clever Impersonator");
        harness.assertInGraveyard(player1, "Clever Impersonator");
    }

    private void castCleverImpersonator() {
        harness.setHand(player1, List.of(new CleverImpersonator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
