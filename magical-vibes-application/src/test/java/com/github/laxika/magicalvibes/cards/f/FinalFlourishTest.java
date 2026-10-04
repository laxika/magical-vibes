package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalFlourish.class, DarksteelRelic.class, HillGiant.class, CopperHostCrusher.class})
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

    @Test
    void kickedSpellReplacesTheReductionAndExpiresAtEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Copper Host Crusher");
        assertThat(target.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getPowerModifier()).isEqualTo(-6);
        assertThat(target.getToughnessModifier()).isEqualTo(-6);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void unkickedReductionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        castFinalFlourish(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Copper Host Crusher");
    }

    @Test
    void canSacrificeTheTargetToPayKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), target.getId());

        harness.assertNotOnBattlefield(player1, "Copper Host Crusher");
        harness.assertInGraveyard(player1, "Copper Host Crusher");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Final Flourish");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsPermanentForKicker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertInHand(player1, "Final Flourish");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotKickWithoutSacrificingAPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new FinalFlourish()));
        addFinalFlourishMana();

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Copper Host Crusher");
        harness.assertInHand(player1, "Final Flourish");
        assertThat(gd.stack).isEmpty();
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
