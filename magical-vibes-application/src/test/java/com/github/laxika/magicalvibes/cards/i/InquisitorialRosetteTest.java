package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorialRosette.class, GrizzlyBears.class})
class InquisitorialRosetteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the equipped creature creates an attacking Astartes Warrior and grants menace")
    void attackTriggerCreatesAttackingTokenAndGrantsMenace() {
        Permanent creature = addCreatureReady(player1);
        Permanent rosette = addRosette(player1);
        rosette.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent token = findPermanent(player1, "Astartes Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace granted by the attack trigger wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent rosette = addRosette(player1);
        rosette.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An unattached Equipment does not trigger when a creature attacks")
    void unattachedEquipmentDoesNotTrigger() {
        addCreatureReady(player1);
        addRosette(player1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Astartes Warrior"));
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addRosette(Player player) {
        Permanent permanent = new Permanent(new InquisitorialRosette());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
