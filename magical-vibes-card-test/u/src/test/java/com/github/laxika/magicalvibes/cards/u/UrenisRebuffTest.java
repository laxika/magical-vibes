package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrenisRebuff.class, AngelicChorus.class, GrizzlyBears.class})
class UrenisRebuffTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToItsOwnersHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UrenisRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new UrenisRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Angelic Chorus")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void harmonizeCastsFromGraveyardAndExilesTheSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UrenisRebuff spell = new UrenisRebuff();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, List.of(target.getId()), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void harmonizeReducesGenericCostByTappedCreaturePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UrenisRebuff spell = new UrenisRebuff();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(target.getId()), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCanTapTheCreatureItWillReturn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        UrenisRebuff spell = new UrenisRebuff();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId()), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCannotTapAnAlreadyTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setGraveyard(player1, List.of(new UrenisRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(target.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature you control");
        harness.assertInGraveyard(player1, "Ureni's Rebuff");
    }

    @Test
    void harmonizeCannotTapAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new UrenisRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(target.getId()), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not on your battlefield");
        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ureni's Rebuff");
    }

    @Test
    void harmonizeCannotTapMoreThanOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new UrenisRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(target.getId()), List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature");
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ureni's Rebuff");
    }
}
