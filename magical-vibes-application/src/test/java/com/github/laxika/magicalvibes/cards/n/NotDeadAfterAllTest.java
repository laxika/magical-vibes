package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NotDeadAfterAll.class, GrizzlyBears.class, DoomBlade.class})
class NotDeadAfterAllTest extends BaseCardTest {

    @Test
    void returnsTappedAndAttachesWickedRole() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        Permanent role = findPermanent(player1, "Wicked");

        assertThat(returned.isTapped()).isTrue();
        assertThat(role.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isTrue();
    }

    @Test
    void wickedRoleCausesOpponentToLoseLifeWhenItDies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        destroy(player2, returned);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    void canTargetOnlyACreatureYouControl() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player1, List.of(new NotDeadAfterAll()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NotDeadAfterAll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroy(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
