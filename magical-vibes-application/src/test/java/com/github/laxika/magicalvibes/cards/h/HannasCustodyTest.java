package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HannasCustodyTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifacts have shroud, on either battlefield")
    void noncreatureArtifactsHaveShroud() {
        harness.addToBattlefield(player1, new HannasCustody());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player2, new IcyManipulator());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Icy Manipulator"), Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Icy Manipulator"), Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures also have shroud")
    void artifactCreaturesHaveShroud() {
        harness.addToBattlefield(player1, new HannasCustody());
        harness.addToBattlefield(player2, new Juggernaut());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Juggernaut"), Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Nonartifact permanents are unaffected")
    void nonArtifactsUnaffected() {
        harness.addToBattlefield(player1, new HannasCustody());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once Hanna's Custody leaves the battlefield")
    void shroudLostWhenCustodyLeaves() {
        harness.addToBattlefield(player1, new HannasCustody());
        Permanent custody = findPermanent(player1, "Hanna's Custody");
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");

        assertThat(gqs.hasKeyword(gd, icy, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(custody);

        assertThat(gqs.hasKeyword(gd, icy, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A shrouded artifact cannot be targeted by a spell")
    void shroudedArtifactCannotBeTargeted() {
        harness.addToBattlefield(player2, new HannasCustody());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID icyId = harness.getPermanentId(player2, "Icy Manipulator");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, icyId))
                .isInstanceOf(IllegalStateException.class);
    }
}
