package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoggartBirthRite.class, BoggartHarbinger.class, Mulldrifter.class, AmoeboidChangeling.class, NamelessInversion.class})
class BoggartBirthRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Boggart Birth Rite returns target Goblin card from graveyard to hand")
    void returnsGoblinFromGraveyardToHand() {
        Card goblin = new BoggartHarbinger();
        harness.setGraveyard(player1, List.of(goblin));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, goblin.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(goblin.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Boggart Birth Rite cannot target non-Goblin card in graveyard")
    void cannotTargetNonGoblinCard() {
        Card creature = new Mulldrifter();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boggart Birth Rite cannot target card in opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card goblin = new BoggartHarbinger();
        harness.setGraveyard(player2, List.of(goblin));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Boggart Birth Rite returns a noncreature Goblin card")
    void returnsNoncreatureGoblinCard() {
        Card target = new BoggartBirthRite();
        Card otherGoblin = new BoggartHarbinger();
        harness.setGraveyard(player1, List.of(target, otherGoblin));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherGoblin).doesNotContain(target);
    }

    @Test
    @DisplayName("Boggart Birth Rite returns a changeling from the graveyard")
    void returnsChangelingCard() {
        Card target = new AmoeboidChangeling();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Boggart Birth Rite does not return another Goblin when its target leaves the graveyard")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        Card target = new BoggartHarbinger();
        Card otherGoblin = new BoggartBirthRite();
        harness.setGraveyard(player1, List.of(target, otherGoblin));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(otherGoblin));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherGoblin);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boggart Birth Rite requires a target even when a Goblin is available")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new BoggartHarbinger()));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boggart Birth Rite returns a kindred instant with changeling")
    void returnsKindredChangelingCard() {
        Card target = new NamelessInversion();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }
}
