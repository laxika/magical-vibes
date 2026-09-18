package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WallOfIce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernHoardDragon.class, Spellbook.class, LeoninScimitar.class,
        Ornithopter.class, GrizzlyBears.class, WallOfIce.class})
class CavernHoardDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less by the greatest number of artifacts controlled by an opponent")
    void costsLessByGreatestOpponentArtifactCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new CavernHoardDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce its cost for artifacts controlled by its controller")
    void doesNotCountControllerArtifactsForCostReduction() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new CavernHoardDragon()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creates a Treasure for each artifact controlled by the damaged player")
    void createsTreasureForEachArtifactControlledByDamagedPlayer() {
        Permanent dragon = addPermanent(player1, new CavernHoardDragon());
        dragon.setAttacking(true);

        addPermanent(player2, new LeoninScimitar());
        addPermanent(player2, new Ornithopter());
        addPermanent(player2, new GrizzlyBears());

        resolveDragonCombat();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not create Treasures when blocked")
    void doesNotCreateTreasureWhenBlocked() {
        Permanent dragon = addPermanent(player1, new CavernHoardDragon());
        dragon.setAttacking(true);
        Permanent blocker = addPermanent(player2, new WallOfIce());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveDragonCombat();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void resolveDragonCombat() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
