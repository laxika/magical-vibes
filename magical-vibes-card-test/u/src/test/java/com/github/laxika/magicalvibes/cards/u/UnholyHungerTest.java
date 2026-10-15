package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnholyHunger.class, GrizzlyBears.class, Shock.class, LavaAxe.class})
class UnholyHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature without spell mastery and gains no life")
    void destroysWithoutSpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        int life = gd.playerLifeTotals.get(player1.getId());

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life);
    }

    @Test
    @DisplayName("Spell mastery gains 2 life in addition to destroying the creature")
    void spellMasteryGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        int life = gd.playerLifeTotals.get(player1.getId());

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life + 2);
    }

    @Test
    @DisplayName("Can destroy a creature you control")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two instant cards satisfy spell mastery")
    void twoInstantsSatisfySpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setLife(player1, 20);

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two sorcery cards satisfy spell mastery")
    void twoSorceriesSatisfySpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LavaAxe(), new LavaAxe()));
        harness.setLife(player1, 20);

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature cards and the opponent's spells do not count toward spell mastery")
    void ignoresNonSpellsAndOpponentsGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock(), new LavaAxe()));
        harness.setLife(player1, 20);

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell mastery checks the graveyard at resolution after a response resolves")
    void spellMasteryChecksAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LavaAxe()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new UnholyHunger(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unholy Hunger");
    }

    @Test
    @DisplayName("No life is gained when the only target dies before resolution")
    void illegalTargetPreventsLifeGain() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new UnholyHunger(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unholy Hunger");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Player player, UUID targetId) {
        harness.setHand(player, List.of(new UnholyHunger()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
