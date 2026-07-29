package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianTributeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two creatures and destroys the target artifact")
    void sacrificesTwoCreaturesAndDestroysArtifact() {
        Permanent firstSacrifice = new Permanent(new GrizzlyBears());
        Permanent secondSacrifice = new Permanent(new GrizzlyBears());
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(firstSacrifice);
        gd.playerBattlefields.get(player1.getId()).add(secondSacrifice);
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot cast while controlling only one creature")
    void cannotCastWithOnlyOneCreature() {
        Permanent onlyCreature = new Permanent(new GrizzlyBears());
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(onlyCreature);
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, artifact.getId(),
                List.of(onlyCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent firstSacrifice = new Permanent(new GrizzlyBears());
        Permanent secondSacrifice = new Permanent(new GrizzlyBears());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(firstSacrifice);
        gd.playerBattlefields.get(player1.getId()).add(secondSacrifice);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new PhyrexianTribute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, creature.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
