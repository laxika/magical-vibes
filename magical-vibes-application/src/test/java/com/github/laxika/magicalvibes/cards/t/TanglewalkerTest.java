package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tanglewalker.class, DarksteelCitadel.class, DarksteelGargoyle.class, DarksteelIngot.class})
class TanglewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tanglewalker makes itself unblockable while defending player controls an artifact land")
    void makesItselfUnblockableWithArtifactLand() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent tanglewalker = addAttackingCreature(player1, new Tanglewalker());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker),
                        battlefieldIndex(player1, tanglewalker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Tanglewalker affects each creature you control")
    void makesOtherCreaturesYouControlUnblockableWithArtifactLand() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent creature = addAttackingCreature(player1, new DarksteelGargoyle());
        harness.addToBattlefield(player1, new Tanglewalker());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker),
                        battlefieldIndex(player1, creature)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("An artifact that is not a land does not enable Tanglewalker's ability")
    void artifactNonlandDoesNotEnableAbility() {
        harness.addToBattlefield(player2, new DarksteelIngot());
        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());
        addAttackingCreature(player1, new Tanglewalker());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tanglewalker does not affect creatures controlled by an opponent")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new Tanglewalker());
        Permanent blocker = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent opponentCreature = addAttackingCreature(player2, new DarksteelGargoyle());

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(battlefieldIndex(player1, blocker),
                        battlefieldIndex(player2, opponentCreature))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tanglewalker ignores artifact lands controlled by the attacking player")
    void artifactLandControlledByAttackerDoesNotEnableAbility() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent tanglewalker = addAttackingCreature(player1, new Tanglewalker());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker),
                        battlefieldIndex(player1, tanglewalker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
