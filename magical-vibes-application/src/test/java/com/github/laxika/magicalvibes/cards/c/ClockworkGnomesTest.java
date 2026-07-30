package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClockworkGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to the target artifact creature")
    void resolvingGrantsShield() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());
        harness.passBothPriorities();

        assertThat(thopter.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's artifact creature")
    void canTargetOpponentArtifactCreature() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player2);

        harness.activateAbility(player1, 0, null, thopter.getId());
        harness.passBothPriorities();

        assertThat(thopter.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Clockwork Gnomes")
    void tapsOnActivation() {
        Permanent gnomes = setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());

        assertThat(gnomes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can regenerate itself")
    void canRegenerateSelf() {
        Permanent gnomes = setupGnomes();

        harness.activateAbility(player1, 0, null, gnomes.getId());
        harness.passBothPriorities();

        assertThat(gnomes.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        setupGnomes();
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        setupGnomes();
        Card artifactCard = new Card();
        artifactCard.setName("Test Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(artifactCard);
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());
        gd.playerBattlefields.get(player1.getId()).remove(thopter);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(thopter.getRegenerationShield()).isZero();
    }

    private Permanent setupGnomes() {
        harness.addToBattlefield(player1, new ClockworkGnomes());
        Permanent gnomes = findPermanent(player1, "Clockwork Gnomes");
        gnomes.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        return gnomes;
    }

    private Permanent addArtifactCreature(Player player) {
        harness.addToBattlefield(player, new Ornithopter());
        return findPermanent(player, "Ornithopter");
    }
}
