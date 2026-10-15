package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.Esperzoa;
import com.github.laxika.magicalvibes.cards.r.ResearchAssistant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChiefEngineer.class, ResearchAssistant.class, BronzeSable.class, Divination.class, Esperzoa.class})
class ChiefEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells can be cast using convoke")
    void grantsConvokeToArtifactSpells() {
        harness.addToBattlefield(player1, new ChiefEngineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ResearchAssistant());
        harness.setHand(player1, List.of(new BronzeSable()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Convoke is not granted to nonartifact spells")
    void doesNotGrantConvokeToNonartifactSpells() {
        harness.addToBattlefield(player1, new ChiefEngineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ResearchAssistant());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void chiefEngineerAndNewCreaturesCanConvokeWithoutMana() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new ChiefEngineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ResearchAssistant());
        engineer.setSummoningSick(true);
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new BronzeSable()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(engineer.getId(), creature.getId()));

        assertThat(engineer.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bronze Sable");
    }

    @Test
    void opponentChiefEngineerDoesNotGrantConvoke() {
        harness.addToBattlefield(player2, new ChiefEngineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ResearchAssistant());
        harness.setHand(player1, List.of(new BronzeSable()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void tappedCreaturesCannotConvoke() {
        harness.addToBattlefield(player1, new ChiefEngineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ResearchAssistant());
        creature.tap();
        harness.setHand(player1, List.of(new BronzeSable()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantEndsWhenChiefEngineerLeavesBattlefield() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new ChiefEngineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ResearchAssistant());
        harness.setHand(player1, List.of(new BronzeSable()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerBattlefields.get(player1.getId()).remove(engineer);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void colorlessCreaturesCannotPayColoredArtifactCost() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new ChiefEngineer());
        engineer.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new Esperzoa()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
    }
}
