package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloseEncounter.class, AirElemental.class})
class CloseEncounterTest extends BaseCardTest {

    @Test
    void dealsChosenCreaturePowerToTargetCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCreature());

        castCloseEncounter(chosen.getId(), target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void usesLastKnownPowerWhenChosenCreatureLeavesBeforeResolution() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCreature());

        castCloseEncounter(chosen.getId(), target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void usesPowerAsItLastExistedWhenChosenCreatureChangesBeforeLeaving() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCreature());

        castCloseEncounterWithoutResolving(chosen.getId(), target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chosen));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void canChooseWarpedCreatureCardOwnedInExile() {
        Card warpedCreature = warpedCreature(5);
        harness.setExile(player1, List.of(warpedCreature));
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCreature());

        castCloseEncounter(warpedCreature.getId(), target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    private void castCloseEncounter(java.util.UUID chosenObjectId, java.util.UUID targetId) {
        castCloseEncounterWithoutResolving(chosenObjectId, targetId);
        harness.passBothPriorities();
    }

    private void castCloseEncounterWithoutResolving(java.util.UUID chosenObjectId, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CloseEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithChosenAdditionalCostObject(player1, 0, targetId, chosenObjectId);
    }

    private Card warpedCreature(int power) {
        Card card = new Card();
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(power);
        card.setKeywords(Set.of(Keyword.WARP));
        return card;
    }

    private Card targetCreature() {
        Card card = new Card();
        card.setType(CardType.CREATURE);
        card.setPower(0);
        card.setToughness(10);
        return card;
    }
}
