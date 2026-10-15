package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreckHunter.class, Forest.class, GrizzlyBears.class, Shock.class})
class WreckHunterTest extends BaseCardTest {

    @Test
    void createsPowerstonesForNonlandCardsThatEnteredTargetPlayersGraveyardFromBattlefield() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock(), new Shock(), new WreckHunter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, firstCreature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, secondCreature.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        List<Permanent> powerstones = findPermanents(player2, "Powerstone");
        assertThat(powerstones).hasSize(2);
        assertThat(powerstones).allSatisfy(powerstone -> assertThat(powerstone.isTapped()).isTrue());
    }

    @Test
    void doesNotCountCardsAlreadyInTargetPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WreckHunter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void cannotTargetACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WreckHunter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
