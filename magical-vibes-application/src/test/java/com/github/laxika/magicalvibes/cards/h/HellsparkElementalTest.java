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

@DisplayName("Hellspark Elemental")
class HellsparkElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        Permanent hellspark = new Permanent(new HellsparkElemental());
        gd.playerBattlefields.get(player1.getId()).add(hellspark);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hellspark Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hellspark Elemental"));
    }

    @Test
    @DisplayName("Unearth returns Hellspark Elemental to the battlefield with haste")
    void unearthReturnsWithHaste() {
        HellsparkElemental card = new HellsparkElemental();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hellspark Elemental"))
                .findFirst().orElseThrow();
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Hellspark Elemental"));
    }

    @Test
    @DisplayName("Unearthed Hellspark Elemental is exiled at the next end step")
    void unearthExiledAtEndStep() {
        HellsparkElemental card = new HellsparkElemental();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hellspark Elemental"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hellspark Elemental"));
    }
}
