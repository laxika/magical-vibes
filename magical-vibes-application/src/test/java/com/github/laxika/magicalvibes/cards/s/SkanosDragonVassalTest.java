package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkanosDragonVassal.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Swamp.class})
class SkanosDragonVassalTest extends BaseCardTest {

    @Test
    void attackTriggerBoostsAnotherAttackingCreatureBySkanosPower() {
        addCreatureReady(player1, new SkanosDragonVassal());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(2);
    }

    @Test
    void whiteFaceGivesTheTargetLifelinkAndUsesFourPower() {
        Permanent skanos = specialize(CardColor.WHITE, 0, new Plains());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackAndChoose(skanos, attacker);

        assertThat(gqs.hasKeyword(gd, skanos, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
    }

    @Test
    void blueFaceGivesTheTargetFlying() {
        Permanent skanos = specialize(CardColor.BLUE, 1, new Island());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackAndChoose(skanos, attacker);

        assertThat(gqs.hasKeyword(gd, skanos, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
    }

    @Test
    void blackFaceHasMenaceAndUsesFivePower() {
        Permanent skanos = specialize(CardColor.BLACK, 2, new Swamp());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackAndChoose(skanos, attacker);

        assertThat(gqs.hasKeyword(gd, skanos, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
    }

    @Test
    void redFaceHasFirstStrikeAndGivesItToTheTarget() {
        Permanent skanos = specialize(CardColor.RED, 3, new Mountain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackAndChoose(skanos, attacker);

        assertThat(gqs.hasKeyword(gd, skanos, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
    }

    @Test
    void greenFaceUntapsTheTargetAndUsesSixPower() {
        Permanent skanos = specialize(CardColor.GREEN, 4, new Forest());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackAndChoose(skanos, attacker);

        assertThat(gqs.hasKeyword(gd, skanos, Keyword.VIGILANCE)).isTrue();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(8);
    }

    private Permanent specialize(CardColor color, int abilityIndex, Card discard) {
        Permanent skanos = addCreatureReady(player1, new SkanosDragonVassal());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return skanos;
    }

    private void attackAndChoose(Permanent skanos, Permanent attacker) {
        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(skanos),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
    }
}
