package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartlessConscription.class, GrizzlyBears.class, MirrorGallery.class})
class HeartlessConscriptionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all creatures, leaves noncreatures, and exiles itself")
    void exilesCreaturesAndItself() {
        harness.addToBattlefield(player1, new MirrorGallery());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HeartlessConscription spell = new HeartlessConscription();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Mirror Gallery");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(player1Creature.getCard())
                .doesNotContain(player2Creature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(player2Creature.getCard());
    }

    @Test
    @DisplayName("Lets its controller cast an opponent's exiled creature with any mana")
    void controllerCanCastOpponentCreatureWithAnyMana() {
        Card creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getCard();
        HeartlessConscription spell = new HeartlessConscription();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }
}
