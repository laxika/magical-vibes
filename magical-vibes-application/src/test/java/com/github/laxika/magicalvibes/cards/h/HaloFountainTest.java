package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaloFountain.class, GrizzlyBears.class, Forest.class})
class HaloFountainTest extends BaseCardTest {

    @Test
    void untapsCreatureAndCreatesCitizenToken() {
        Permanent fountain = addFountain();
        Permanent creature = addTappedCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    void untapsTwoCreaturesAndDrawsCard() {
        Permanent fountain = addFountain();
        Permanent firstCreature = addTappedCreature(player1);
        Permanent secondCreature = addTappedCreature(player1);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void untapsFifteenCreaturesAndWinsTheGame() {
        addFountain();
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            creatures.add(addTappedCreature(player1));
        }
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isFalse());
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void cannotActivateWithoutEnoughTappedCreatures() {
        addFountain();
        addTappedCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFountain() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new HaloFountain());
        fountain.setSummoningSick(false);
        return fountain;
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.tap();
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
