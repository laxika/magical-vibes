package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrillianceUnleashed.class, GrizzlyBears.class, MyrRetriever.class, SolRing.class})
class BrillianceUnleashedTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void returnsNoncreatureArtifactAsRobotCreatureWithFlying() {
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));

        cast(1, artifact.getId());

        Permanent returned = findPermanent(player1, "Sol Ring");
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    void returnsArtifactCreatureWithoutAnimatingIt() {
        Card artifactCreature = new MyrRetriever();
        harness.setGraveyard(player1, List.of(artifactCreature));

        cast(1, artifactCreature.getId());

        Permanent returned = findPermanent(player1, "Myr Retriever");
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(returned.getGrantedSubtypes()).doesNotContain(CardSubtype.ROBOT);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotTargetNonartifactCardWithTheReturnMode() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> cast(1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesResolveWithSeparateTargets() {
        Permanent creatureTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));

        castBoth(artifact.getId(), creatureTarget.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Sol Ring"))).isTrue();
    }

    @Test
    void cannotReturnArtifactFromOpponentsGraveyard() {
        Card artifact = new SolRing();
        harness.setGraveyard(player2, List.of(artifact));

        assertThatThrownBy(() -> cast(1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeCannotTargetPlayerOrNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        assertThatThrownBy(() -> cast(0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cast(0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeStillResolvesWhenDamageTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new BrillianceUnleashed()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), artifact.getId()), List.of());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Sol Ring");
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    void damageModeStillResolvesWhenGraveyardTargetIsExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new BrillianceUnleashed()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), artifact.getId()), List.of());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(artifact));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void robotAnimationPersistsIntoNextTurnAndPreservesManaAbility() {
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        cast(1, artifact.getId());

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.passUntil(player1, TurnStep.UPKEEP);

        Permanent returned = findPermanent(player1, "Sol Ring");
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BrillianceUnleashed()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId), List.of());
        harness.passBothPriorities();
    }

    private void castBoth(java.util.UUID graveyardTarget, java.util.UUID permanentTarget) {
        harness.setHand(player1, List.of(new BrillianceUnleashed()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(permanentTarget, graveyardTarget), List.of());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
