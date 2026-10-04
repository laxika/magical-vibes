package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.c.CrazedArmodon;
import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HannasCustody.class, Disenchant.class, CursedScroll.class, BottleGnomes.class,
        CrazedArmodon.class, LiquimetalCoating.class})
class HannasCustodyTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifacts have shroud, on either battlefield")
    void noncreatureArtifactsHaveShroud() {
        harness.addToBattlefield(player1, new HannasCustody());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());

        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingArtifact, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures also have shroud")
    void artifactCreaturesHaveShroud() {
        harness.addToBattlefield(player1, new HannasCustody());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Nonartifact permanents are unaffected")
    void nonArtifactsUnaffected() {
        harness.addToBattlefield(player1, new HannasCustody());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player2, new CrazedArmodon());

        assertThat(gqs.hasKeyword(gd, nonArtifact, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once Hanna's Custody leaves the battlefield")
    void shroudLostWhenCustodyLeaves() {
        Permanent custody = harness.addToBattlefieldAndReturn(player1, new HannasCustody());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(custody);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A shrouded artifact cannot be targeted by a spell")
    void shroudedArtifactCannotBeTargeted() {
        harness.addToBattlefield(player2, new HannasCustody());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A shrouded artifact cannot be targeted by an activated ability")
    void shroudedArtifactCannotBeTargetedByAbility() {
        harness.addToBattlefield(player1, new HannasCustody());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new CursedScroll()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud also prevents the artifact's controller from targeting it")
    void controllerCannotTargetOwnArtifact() {
        harness.addToBattlefield(player2, new HannasCustody());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("An artifact gaining shroud becomes an illegal target before resolution")
    void gainingShroudStopsPendingSpell() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, artifact.getId());

        harness.addToBattlefield(player2, new HannasCustody());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cursed Scroll");
        harness.assertInGraveyard(player1, "Disenchant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud does not prevent an artifact's nontargeting sacrifice ability")
    void shroudedArtifactCanSacrificeItself() {
        harness.addToBattlefield(player1, new HannasCustody());
        harness.addToBattlefield(player1, new BottleGnomes());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bottle Gnomes");
        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    @DisplayName("A nonartifact gains shroud when it becomes an artifact")
    void newArtifactGainsShroud() {
        harness.addToBattlefield(player1, new HannasCustody());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrazedArmodon());
        harness.addToBattlefield(player1, new LiquimetalCoating());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Hanna's Custody grants itself shroud if it becomes an artifact")
    void custodyHasShroudWhenItBecomesArtifact() {
        Permanent custody = harness.addToBattlefieldAndReturn(player1, new HannasCustody());
        harness.addToBattlefield(player1, new LiquimetalCoating());

        harness.activateAbility(player1, 1, null, custody.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, custody, Keyword.SHROUD)).isTrue();
    }
}
