package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CursedTotem;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianTribute.class, FeralShadow.class, CursedTotem.class})
class PhyrexianTributeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two creatures and destroys the target artifact")
    void sacrificesTwoCreaturesAndDestroysArtifact() {
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Feral Shadow");
        harness.assertNotOnBattlefield(player2, "Cursed Totem");
        harness.assertInGraveyard(player2, "Cursed Totem");
    }

    @Test
    @DisplayName("Cannot cast while controlling only one creature")
    void cannotCastWithOnlyOneCreature() {
        Permanent onlyCreature = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(onlyCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertOnBattlefield(player2, "Cursed Totem");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralShadow());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, creature.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Feral Shadow");
    }

    @Test
    @DisplayName("Cannot cast without choosing an artifact target")
    void cannotCastWithoutArtifactTarget() {
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                List.of(firstSacrifice.getId(), secondSacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertOnBattlefield(player2, "Cursed Totem");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FeralShadow());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(ownCreature.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertOnBattlefield(player2, "Feral Shadow");
        harness.assertOnBattlefield(player2, "Cursed Totem");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreaturePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new CursedTotem());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(creature.getId(), noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertOnBattlefield(player1, "Cursed Totem");
        harness.assertOnBattlefield(player2, "Cursed Totem");
    }

    @Test
    @DisplayName("Cannot sacrifice the same creature twice")
    void cannotSacrificeSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertOnBattlefield(player2, "Cursed Totem");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the caster")
    void canDestroyOwnArtifact() {
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId()));
        harness.assertNotOnBattlefield(player1, "Feral Shadow");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Feral Shadow");
        harness.assertNotOnBattlefield(player1, "Cursed Totem");
        harness.assertInGraveyard(player1, "Cursed Totem");
    }

    @Test
    @DisplayName("Does not destroy an artifact that leaves before resolution")
    void doesNotDestroyArtifactThatLeavesBeforeResolution() {
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedTotem());

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId()));
        harness.assertNotOnBattlefield(player1, "Feral Shadow");
        gd.playerBattlefields.get(player2.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
        harness.assertInGraveyard(player1, "Phyrexian Tribute");
    }
}
