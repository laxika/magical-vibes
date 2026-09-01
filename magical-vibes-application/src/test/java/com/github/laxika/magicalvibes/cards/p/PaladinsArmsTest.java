package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaladinsArms.class, GrizzlyBears.class, Shock.class, GiantGrowth.class})
class PaladinsArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Paladin's Arms creates and equips a Hero token")
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new PaladinsArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent arms = findPermanent(player1, "Paladin's Arms");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(arms.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO, CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Equip moves Paladin's Arms and its bonuses to another creature")
    void equipMovesArms() {
        Permanent arms = addArmsReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        arms.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(arms.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Ward {1} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent arms = addArmsReady(player1);
        Permanent equipped = addCreatureReady(player1);
        arms.setAttachedTo(equipped.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, equipped.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying Ward {1} lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent arms = addArmsReady(player1);
        Permanent equipped = addCreatureReady(player1);
        arms.setAttachedTo(equipped.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, equipped.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(6);
    }

    private Permanent addArmsReady(Player player) {
        Permanent permanent = new Permanent(new PaladinsArms());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
