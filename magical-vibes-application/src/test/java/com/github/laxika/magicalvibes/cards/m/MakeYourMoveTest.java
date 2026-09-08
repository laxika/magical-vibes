package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MakeYourMoveTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact regardless of power")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        castOn("Fountain of Youth");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys a target enchantment regardless of power")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        castOn("Angelic Chorus");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys a target creature with power 4 or greater")
    void destroysLargeCreature() {
        harness.addToBattlefield(player2, new CrawWurm());
        castOn("Craw Wurm");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    private void castOn(String permanentName) {
        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, permanentName));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
