package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandUpForYourself.class, HillGiant.class, GrizzlyBears.class, CrawWurm.class, Forest.class})
class StandUpForYourselfTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power 3 or greater")
    void destroysHighPowerCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 3")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CrawWurm());
        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");

        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Stand Up for Yourself");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void canTargetCreatureWhoseEffectivePowerIsThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(1);
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureWhoseEffectivePowerIsBelowThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(-1);
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    void doesNotDestroyTargetWhosePowerFallsBelowThreeBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Stand Up for Yourself");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    void allowsRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Stand Up for Yourself");
    }

    @Test
    void doesNotDestroyIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new StandUpForYourself()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Stand Up for Yourself");
    }
}
