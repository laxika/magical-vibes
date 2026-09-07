package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndyingMalice.class, DoomBlade.class, GrizzlyBears.class})
class UndyingMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the creature tapped under its owner's control with a +1/+1 counter")
    void returnsTappedWithCounterUnderOwnersControl() {
        Permanent target = addCreature(player2);
        Card targetCard = target.getCard();

        castUndyingMalice(player1, target.getId());
        castDoomBlade(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("The granted death trigger expires at end of turn")
    void deathTriggerExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        castUndyingMalice(player1, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castDoomBlade(player2, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castUndyingMalice(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new UndyingMalice()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private void castDoomBlade(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
