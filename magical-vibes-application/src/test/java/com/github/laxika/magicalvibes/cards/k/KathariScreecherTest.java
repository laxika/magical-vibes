package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Kathari Screecher")
class KathariScreecherTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Kathari Screecher to the battlefield with haste")
    void unearthReturnsWithHaste() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kathari Screecher"))
                .findFirst().orElseThrow();
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Kathari Screecher"));
    }

    @Test
    @DisplayName("Unearthed Kathari Screecher is exiled at the next end step")
    void unearthExiledAtEndStep() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kathari Screecher"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Screecher"));
    }

    @Test
    @DisplayName("Unearth can only be activated at sorcery speed")
    void unearthOnlyAtSorcerySpeed() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Screecher"));
    }

    @Test
    @DisplayName("Unearthed Kathari Screecher is exiled if it would leave the battlefield")
    void unearthExiledIfWouldLeaveBattlefield() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kathari Screecher"))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, perm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kathari Screecher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Kathari Screecher"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Screecher"));
    }
}
