package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({AntManReformedRogue.class, GrizzlyBears.class, LlanowarElves.class,
        Shock.class, Unsummon.class, CoilingOracle.class})
class AntManReformedRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a green spell gives Ant-Man +1/+0 and trample until end of turn")
    void greenSpellBoostsAndGrantsTrample() {
        Permanent antMan = addCreatureReady(player1, new AntManReformedRogue());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, antMan)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, antMan, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, antMan, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting a blue spell gives Ant-Man -1/-0 and makes it unblockable until end of turn")
    void blueSpellShrinksAndMakesUnblockable() {
        Permanent antMan = addCreatureReady(player1, new AntManReformedRogue());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, antMan)).isEqualTo(3);
        assertThat(antMan.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(2);
        assertThat(antMan.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-green, non-blue spell does not trigger Ant-Man")
    void otherColorsDoNotTrigger() {
        Permanent antMan = addCreatureReady(player1, new AntManReformedRogue());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, antMan)).isEqualTo(3);
        assertThat(antMan.isCantBeBlocked()).isFalse();
        assertThat(gqs.hasKeyword(gd, antMan, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player draws a card")
    void combatDamageDrawsCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AntManReformedRogue());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void greenBlueSpellTriggersBothAbilities() {
        Permanent antMan = addCreatureReady(player1, new AntManReformedRogue());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CoilingOracle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, antMan)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, antMan, Keyword.TRAMPLE)).isTrue();
        assertThat(antMan.isCantBeBlocked()).isTrue();
    }

    @Test
    void repeatedBlueSpellsReducePowerToZeroAndCombatDoesNotDraw() {
        Permanent antMan = addCreatureReady(player1, new AntManReformedRogue());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, antMan)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, antMan)).isEqualTo(3);
        assertThat(antMan.isCantBeBlocked()).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsGreenBlueSpellDoesNotTriggerEitherAbility() {
        Permanent antMan = addCreatureReady(player1, new AntManReformedRogue());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new CoilingOracle()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, antMan)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, antMan, Keyword.TRAMPLE)).isFalse();
        assertThat(antMan.isCantBeBlocked()).isFalse();
    }
}
