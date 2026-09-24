package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreyasApprentice.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class BreyasApprenticeTest extends BaseCardTest {

    @Test
    void createsAThopterWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new BreyasApprentice());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    void exilesTheTopCardAndAllowsPlayingItUntilNextTurn() {
        Permanent apprentice = addReadyApprentice();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(apprentice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apprentice);
    }

    @Test
    void boostsTargetCreatureAndSacrificesAnArtifact() {
        Permanent apprentice = addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apprentice);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(apprentice.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    void boostModeRejectsANonCreatureTarget() {
        addReadyApprentice();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyApprentice() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new BreyasApprentice());
        apprentice.setSummoningSick(false);
        return apprentice;
    }
}
