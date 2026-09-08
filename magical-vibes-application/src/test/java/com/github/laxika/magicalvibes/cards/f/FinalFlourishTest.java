package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalFlourish.class, DarksteelRelic.class, HillGiant.class})
class FinalFlourishTest extends BaseCardTest {

    @Test
    void withoutKickerGivesTargetCreatureMinusTwoMinusTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castFinalFlourish(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void withKickerSacrificesArtifactAndGivesTargetCreatureMinusSixMinusSix() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFinalFlourish(UUID targetId) {
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addFinalFlourishMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
