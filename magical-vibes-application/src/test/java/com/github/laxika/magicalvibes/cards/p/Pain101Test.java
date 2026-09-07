package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pain101.class, DoomBlade.class, Forest.class, GrizzlyBears.class})
class Pain101Test extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent creature = addCreature(player1);

        castOn(creature);

        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Returns the targeted creature tapped under its owner's control when it dies")
    void returnsTappedUnderOwnersControl() {
        Permanent creature = addCreature(player2);
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()) && p.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The grants wear off at end of turn")
    void grantsWearOffAtEndOfTurn() {
        Permanent creature = addCreature(player1);
        Card creatureCard = creature.getCard();

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        destroy(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Pain101()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        harness.addToBattlefield(player, new GrizzlyBears());
        return gd.playerBattlefields.get(player.getId()).getLast();
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Pain101()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
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
