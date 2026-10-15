package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.r.RunAground;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevilDinosaur.class, RaptorCompanion.class, BishopsSoldier.class, RunAground.class})
class DevilDinosaurTest extends BaseCardTest {

    @Test
    void buffsOtherDinosaursAndGrantsThemHexproof() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        int basePower = gqs.getEffectivePower(gd, raptor);
        int baseToughness = gqs.getEffectiveToughness(gd, raptor);

        harness.addToBattlefield(player1, new DevilDinosaur());

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void doesNotAffectItselfNonDinosaursOrOpponents() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        Permanent opponentRaptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        int opponentPower = gqs.getEffectivePower(gd, opponentRaptor);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponentRaptor);

        Permanent devil = harness.addToBattlefieldAndReturn(player1, new DevilDinosaur());

        assertThat(gqs.hasKeyword(gd, devil, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentRaptor)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentRaptor)).isEqualTo(opponentToughness);
        assertThat(gqs.hasKeyword(gd, opponentRaptor, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void hexproofStopsOpponentsFromTargetingOtherDinosaurs() {
        harness.addToBattlefield(player1, new DevilDinosaur());
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, raptor.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Raptor Companion");
    }

    @Test
    void controllerCanStillTargetTheirOtherDinosaurs() {
        harness.addToBattlefield(player1, new DevilDinosaur());
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new RunAground()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, raptor.getId());

        harness.assertNotOnBattlefield(player1, "Raptor Companion");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(raptor.getCard().getId());
    }

    @Test
    void bonusesEndWhenDevilDinosaurLeavesTheBattlefield() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        int basePower = gqs.getEffectivePower(gd, raptor);
        int baseToughness = gqs.getEffectiveToughness(gd, raptor);
        Permanent devil = harness.addToBattlefieldAndReturn(player1, new DevilDinosaur());
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HEXPROOF)).isTrue();
        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player2, 0, devil.getId());

        harness.assertNotOnBattlefield(player1, "Devil Dinosaur");
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HEXPROOF)).isFalse();
        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player2, 0, raptor.getId());

        harness.assertNotOnBattlefield(player1, "Raptor Companion");
    }

    @Test
    void bonusesStartOnlyWhenTheCreatureSpellResolves() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        int basePower = gqs.getEffectivePower(gd, raptor);
        int baseToughness = gqs.getEffectiveToughness(gd, raptor);

        harness.castFromHand(player1, new DevilDinosaur(), "{2}{G}{G}");

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devil Dinosaur");
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HEXPROOF)).isTrue();
    }
}
