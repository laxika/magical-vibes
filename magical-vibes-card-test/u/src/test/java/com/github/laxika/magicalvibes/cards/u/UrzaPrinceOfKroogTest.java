package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzaPrinceOfKroog.class, Ornithopter.class, HowlingMine.class})
class UrzaPrinceOfKroogTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact creatures you control get +2/+2")
    void boostsControlledArtifactCreatures() {
        harness.addToBattlefield(player1, new UrzaPrinceOfKroog());
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opposingArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, ownArtifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifactCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingArtifactCreature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opposingArtifactCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a 1/1 Soldier creature token copy of a controlled artifact")
    void createsArtifactTokenCopy() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        urza.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Howling Mine");
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an artifact controlled by an opponent")
    void cannotTargetOpponentsArtifact() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        urza.setSummoningSick(false);
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Urza copies a creature with its abilities and subtypes but without counters")
    void copiesArtifactCreatureWithoutCounters() {
        harness.addToBattlefield(player1, new UrzaPrinceOfKroog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        artifact.tap();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER, CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Urza does not boost nonartifact creatures or noncreature artifacts")
    void doesNotBoostOtherPermanents() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());

        assertThat(gqs.getEffectivePower(gd, urza)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, urza)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent you control")
    void cannotTargetNonartifact() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, urza.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The copy ability resolves after Urza leaves the battlefield")
    void resolvesWithoutUrza() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(urza);
        gd.playerGraveyards.get(player1.getId()).add(urza.getCard());

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact no longer controlled by you is an illegal target on resolution")
    void doesNotCopyArtifactThatChangesController() {
        harness.addToBattlefield(player1, new UrzaPrinceOfKroog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Urza can activate repeatedly and copy a token created by the first activation")
    void copiesAnExistingToken() {
        harness.addToBattlefield(player1, new UrzaPrinceOfKroog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        Permanent firstToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, null, firstToken.getId());
        harness.passBothPriorities();

        var tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER, CardSubtype.SOLDIER);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("No token is created when the target leaves before resolution")
    void doesNotCopyMissingTarget() {
        harness.addToBattlefield(player1, new UrzaPrinceOfKroog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
