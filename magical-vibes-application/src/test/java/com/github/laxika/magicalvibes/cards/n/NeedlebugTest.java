package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({Needlebug.class, IronMyr.class, BrownOuphe.class, Bonesplitter.class})
class NeedlebugTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during combat thanks to flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Needlebug(), "{4}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent needlebug = addReadyPermanent(player1, new Needlebug(), true);
        Permanent ironMyr = addReadyPermanent(player2, new IronMyr(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, ironMyr), indexOf(player1, needlebug)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent needlebug = addReadyPermanent(player1, new Needlebug(), true);
        Permanent creature = addReadyPermanent(player2, new BrownOuphe(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, needlebug))));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact combat damage")
    void protectionPreventsArtifactCombatDamage() {
        Permanent needlebug = addReadyPermanent(player1, new Needlebug(), false);
        Permanent artifactAttacker = addReadyPermanent(player2, new IronMyr(), true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, needlebug), indexOf(player2, artifactAttacker))));
        harness.passBothPriorities();

        assertThat(needlebug.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifactAttacker);
    }

    @Test
    @DisplayName("Protection from artifacts does not prevent non-artifact combat damage")
    void protectionAllowsNonArtifactCombatDamage() {
        Permanent needlebug = addReadyPermanent(player1, new Needlebug(), false);
        Permanent creature = addReadyPermanent(player2, new BrownOuphe(), true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, needlebug), indexOf(player2, creature))));
        harness.passBothPriorities();

        assertThat(needlebug.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact abilities from targeting Needlebug")
    void protectionPreventsArtifactAbilityTargetingNeedlebug() {
        Permanent needlebug = addReadyPermanent(player1, new Needlebug(), false);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, equipment), null, needlebug.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        Permanent needlebug = addCreatureReady(player1, new Needlebug());
        needlebug.resetModifiers();

        Permanent artifactSource = new Permanent(new IronMyr());
        Permanent nonArtifactSource = new Permanent(new BrownOuphe());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, needlebug, artifactSource)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, needlebug, nonArtifactSource)).isFalse();
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
