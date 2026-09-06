package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hell's Thunder")
class HellsThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        Permanent hellsThunder = new Permanent(new HellsThunder());
        gd.playerBattlefields.get(player1.getId()).add(hellsThunder);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        harness.assertInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearth returns Hell's Thunder to the battlefield with haste")
    void unearthReturnsWithHaste() {
        HellsThunder card = new HellsThunder();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Hell's Thunder");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearthed Hell's Thunder is exiled at the next end step")
    void unearthExiledAtEndStep() {
        HellsThunder card = new HellsThunder();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hell's Thunder"));
    }
}
