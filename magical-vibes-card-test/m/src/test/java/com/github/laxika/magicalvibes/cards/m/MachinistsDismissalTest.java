package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MachinistsDismissal.class, GrizzlyBears.class,
        MightOfOaks.class, Spellbook.class, Ornithopter.class})
class MachinistsDismissalTest extends BaseCardTest {

    @Test
    void countersNoncreatureSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    void costsOneLessForEachControlledArtifactAndArtifactCardInHand() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(
                new MachinistsDismissal(), new Spellbook(), new Spellbook()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void countersNoncreatureArtifactSpell() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player1, List.of(spellbook));
        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Machinist's Dismissal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void excessArtifactsCannotReduceTheBlueManaRequirement() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player1, List.of(spellbook));
        harness.setHand(player2, List.of(new MachinistsDismissal(),
                new Spellbook(), new Spellbook(), new Spellbook(),
                new Spellbook(), new Spellbook()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spellbook.getId()))
                .isInstanceOf(Exception.class);
        harness.assertInHand(player2, "Machinist's Dismissal");

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void opponentArtifactsDoNotReduceTheCost() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        Spellbook spellbook = new Spellbook();
        harness.setHand(player1, List.of(spellbook, new Spellbook(), new Spellbook()));
        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spellbook.getId()))
                .isInstanceOf(Exception.class);
        harness.assertInHand(player2, "Machinist's Dismissal");

        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void artifactsInGraveyardAndExileDoNotReduceTheCost() {
        harness.setGraveyard(player2, List.of(new Spellbook(), new Spellbook()));
        harness.setExile(player2, List.of(new Spellbook(), new Spellbook()));
        Spellbook spellbook = new Spellbook();
        harness.setHand(player1, List.of(spellbook));
        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spellbook.getId()))
                .isInstanceOf(Exception.class);
        harness.assertInHand(player2, "Machinist's Dismissal");

        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void controlledArtifactCreaturesReduceTheCost() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        Spellbook spellbook = new Spellbook();
        harness.setHand(player1, List.of(spellbook));
        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void cannotTargetArtifactCreatureSpell() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setHand(player2, List.of(new MachinistsDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ornithopter.getId()))
                .isInstanceOf(Exception.class);
        harness.assertInHand(player2, "Machinist's Dismissal");
    }
}
