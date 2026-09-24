package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

        assertThat(mirror.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cursed Mirror can temporarily copy a creature with haste")
    void copiesCreatureWithHasteUntilEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CursedMirror()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent mirror = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mirror.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mirror.getCard().getPower()).isEqualTo(2);
        assertThat(mirror.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, mirror)).isTrue();
        assertThat(gqs.isArtifact(gd, mirror)).isFalse();
        assertThat(gqs.hasKeyword(gd, mirror, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Cursed Mirror");
        assertThat(gqs.isCreature(gd, mirror)).isFalse();
        assertThat(gqs.isArtifact(gd, mirror)).isTrue();
        assertThat(gqs.hasKeyword(gd, mirror, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining the copy leaves Cursed Mirror as an artifact")
    void mayDeclineCopy() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CursedMirror()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent mirror = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isArtifact(gd, mirror)).isTrue();
        assertThat(gqs.isCreature(gd, mirror)).isFalse();
    }
}
