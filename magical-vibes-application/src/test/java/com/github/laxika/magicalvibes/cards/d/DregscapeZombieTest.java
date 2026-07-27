package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dregscape Zombie")
class DregscapeZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Dregscape Zombie to the battlefield with haste")
    void unearthReturnsWithHaste() {
        DregscapeZombie card = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Dregscape Zombie");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Unearthed Dregscape Zombie is exiled at the next end step")
    void unearthExiledAtEndStep() {
        DregscapeZombie card = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dregscape Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dregscape Zombie"));
    }
}
