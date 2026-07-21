package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving exiles other spells on the stack")
    void exilesOtherSpells() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new SummaryDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Lightning Bolt"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Summary Dismissal"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exiles uncounterable spells (they still leave the stack)")
    void exilesUncounterableSpells() {
        VolcanicFallout fallout = new VolcanicFallout();
        harness.setHand(player1, List.of(fallout));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new SummaryDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Volcanic Fallout"));
        // Fallout never resolved — no damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counters activated abilities on the stack")
    void countersActivatedAbilities() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();
        icy.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, bearsId);

        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        harness.setHand(player1, List.of(new SummaryDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With empty stack besides itself, resolves with no targets and goes to graveyard")
    void resolvesAlone() {
        harness.setHand(player1, List.of(new SummaryDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Summary Dismissal"));
    }
}
