package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.c.CrazedArmodon;
import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
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

@CardUsed({HannasCustody.class, Disenchant.class, CursedScroll.class, BottleGnomes.class, CrazedArmodon.class})
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());
        harness.setHand(player1, List.of(new CursedScroll()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
