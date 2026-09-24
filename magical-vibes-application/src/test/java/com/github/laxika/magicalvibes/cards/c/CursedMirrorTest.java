package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedMirror.class, GrizzlyBears.class})
class CursedMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Cursed Mirror adds red mana")
    void tapsForRedMana() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new CursedMirror());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mirror.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May enter as a hasty creature copy until end of turn")
    void copiesCreatureUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CursedMirror()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent mirror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Cursed Mirror"))
                .findFirst()
                .orElseThrow();
        assertThat(mirror.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mirror.getCard().getPower()).isEqualTo(2);
        assertThat(mirror.getCard().getToughness()).isEqualTo(2);
        assertThat(mirror.getCard().getKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Cursed Mirror");
    }

    @Test
    @DisplayName("Declining the copy leaves Cursed Mirror as an artifact")
    void declinesCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CursedMirror()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent mirror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Cursed Mirror"))
                .findFirst()
                .orElseThrow();
        int mirrorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mirror);
        harness.tapPermanent(player1, mirrorIndex);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
