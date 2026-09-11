package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlamdringFoeHammer.class, GleamOfDeath.class, GrizzlyBears.class, LavaAxe.class, Shock.class})
class GlamdringFoeHammerTest extends BaseCardTest {

    @Test
    void reducesInstantAndSorceryCostsByEquippedCreaturePower() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = addReadyGlamdring(player1);
        glamdring.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void adventureMillsSixAndReturnsAllInstantAndSorceryCards() {
        GlamdringFoeHammer card = new GlamdringFoeHammer();
        Card instantOne = new Shock();
        Card creatureOne = new GrizzlyBears();
        Card sorcery = new LavaAxe();
        Card creatureTwo = new GrizzlyBears();
        Card instantTwo = new Shock();
        Card creatureThree = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(card)));
        harness.setLibrary(player1, List.of(
                instantOne, creatureOne, sorcery, creatureTwo, instantTwo, creatureThree));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(instantOne, sorcery, instantTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creatureOne, creatureTwo, creatureThree);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCardCanBeCastFromExileAfterResolving() {
        GlamdringFoeHammer card = new GlamdringFoeHammer();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    private Permanent addReadyGlamdring(com.github.laxika.magicalvibes.model.Player player) {
        Permanent glamdring = new Permanent(new GlamdringFoeHammer());
        glamdring.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(glamdring);
        return glamdring;
    }
}
