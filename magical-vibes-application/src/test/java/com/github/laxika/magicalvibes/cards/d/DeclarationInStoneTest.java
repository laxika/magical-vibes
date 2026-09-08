package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeclarationInStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles same-name creatures only from the target controller and investigates for nontokens")
    void exilesControlledSameNameCreaturesAndInvestigatesForNontokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears tokenBear = new GrizzlyBears();
        tokenBear.setToken(true);
        harness.addToBattlefield(player2, tokenBear);
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create Clues when the target is removed before resolution")
    void doesNotResolveWhenTargetLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }
}
