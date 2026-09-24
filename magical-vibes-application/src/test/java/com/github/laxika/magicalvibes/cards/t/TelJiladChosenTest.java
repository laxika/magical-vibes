package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.b.BrownOuphe;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
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

@CardUsed({TelJiladChosen.class, IronMyr.class, BrownOuphe.class, Bonesplitter.class})
class TelJiladChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent chosen = addReadyPermanent(player1, new TelJiladChosen(), true);
        Permanent artifactCreature = addReadyPermanent(player2, new IronMyr(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactCreature), indexOf(player1, chosen)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent chosen = addReadyPermanent(player1, new TelJiladChosen(), true);
        Permanent creature = addReadyPermanent(player2, new BrownOuphe(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, chosen))));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact creatures from dealing combat damage")
    void protectionPreventsArtifactCreatureFromDealingCombatDamage() {
        Permanent chosen = addReadyPermanent(player1, new TelJiladChosen(), false);
        Permanent artifactCreature = addReadyPermanent(player2, new IronMyr(), true);

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, chosen), indexOf(player2, artifactCreature))));
        harness.passBothPriorities();

        assertThat(chosen.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifactCreature);
    }

    @Test
    @DisplayName("Protection from artifacts does not prevent non-artifact creatures from dealing combat damage")
    void protectionAllowsNonArtifactCreatureToDealCombatDamage() {
        Permanent chosen = addReadyPermanent(player1, new TelJiladChosen(), false);
        Permanent creature = addReadyPermanent(player2, new BrownOuphe(), true);

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, chosen), indexOf(player2, creature))));
        harness.passBothPriorities();

        assertThat(chosen.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact abilities from targeting Tel-Jilad Chosen")
    void protectionPreventsArtifactAbilityFromTargetingChosen() {
        Permanent chosen = addReadyPermanent(player1, new TelJiladChosen(), false);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, equipment), null, chosen.getId()))
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
