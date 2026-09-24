package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladArchers.class, LeoninSkyhunter.class, Ornithopter.class, FangrenHunter.class,
        AlphaMyr.class, Bonesplitter.class})
class TelJiladArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Tel-Jilad Archers block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addReadyPermanent(player1, new LeoninSkyhunter(), true);
        Permanent archer = addReadyPermanent(player2, new TelJiladArchers(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archer), indexOf(player1, flyer))));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact creatures from blocking")
    void artifactCreatureCannotBlockArchers() {
        Permanent archer = addReadyPermanent(player1, new TelJiladArchers(), true);
        Permanent artifactCreature = addReadyPermanent(player2, new Ornithopter(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactCreature), indexOf(player1, archer)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts does not prevent non-artifact creatures from blocking")
    void nonArtifactCreatureCanBlockArchers() {
        Permanent archer = addReadyPermanent(player1, new TelJiladArchers(), true);
        Permanent creature = addReadyPermanent(player2, new FangrenHunter(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, archer))));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact creatures from dealing combat damage")
    void artifactCreatureCannotDealCombatDamageToArchers() {
        Permanent archer = addReadyPermanent(player1, new TelJiladArchers(), false);
        Permanent artifactAttacker = addReadyPermanent(player2, new AlphaMyr(), true);

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, archer), indexOf(player2, artifactAttacker))));
        harness.passBothPriorities();

        assertThat(archer.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifactAttacker);
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact abilities from targeting Archers")
    void artifactAbilityCannotTargetArchers() {
        Permanent archer = addReadyPermanent(player1, new TelJiladArchers(), false);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, equipment), null, archer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private Permanent addReadyPermanent(Player player, Card card, boolean attacking) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(attacking);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
