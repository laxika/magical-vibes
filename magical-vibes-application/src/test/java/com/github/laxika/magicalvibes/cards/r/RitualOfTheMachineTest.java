package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.l.LimDLsHighGuard;
import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RitualOfTheMachine.class, ElvishRanger.class, StormCrow.class, LimDLsHighGuard.class,
        AesthirGlider.class, SchoolOfTheUnseen.class})
class RitualOfTheMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and permanently steals the target")
    void stealsTargetCreature() {
        Permanent fodder = addCreatureReady(player1, new ElvishRanger());
        Permanent stolen = addCreatureReady(player2, new StormCrow());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, stolen.getId(), fodder.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, fodder.getCard().getName());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(stolen);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolen);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent fodder = addCreatureReady(player1, new ElvishRanger());
        Permanent blackCreature = addCreatureReady(player2, new LimDLsHighGuard());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, blackCreature.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent fodder = addCreatureReady(player1, new ElvishRanger());
        Permanent glider = addCreatureReady(player2, new AesthirGlider());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, glider.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent fodder = addCreatureReady(player1, new ElvishRanger());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SchoolOfTheUnseen());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        Permanent stolen = addCreatureReady(player2, new StormCrow());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, stolen.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not pay the sacrifice cost when the target is illegal")
    void doesNotPaySacrificeCostForIllegalTarget() {
        Permanent fodder = addCreatureReady(player1, new ElvishRanger());
        Permanent blackCreature = addCreatureReady(player2, new LimDLsHighGuard());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, blackCreature.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInHand(player1, "Ritual of the Machine");
        harness.assertNotInGraveyard(player1, "Elvish Ranger");
    }
}
