package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Flameshape;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SageOfFables;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GandalfGoblinsBaneFlameshape.class, Flameshape.class, Island.class, SageOfFables.class,
        Shock.class})
class GandalfGoblinsBaneFlameshapeTest extends BaseCardTest {

    @Test
    void noncreatureSpellBoostsGandalfAndDamagesEachOpponent() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void creatureSpellDoesNotTriggerGandalf() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        harness.setHand(player1, List.of(new SageOfFables()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void flameshapeExilesTopTwoFaceDownAndRequiresWizardToPlayThem() {
        Card first = new Shock();
        Card second = new Island();
        GandalfGoblinsBaneFlameshape card = new GandalfGoblinsBaneFlameshape();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new SageOfFables());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, first.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void opponentCastingNoncreatureSpellDoesNotTriggerGandalf() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void eachCastAddsABoostThatExpiresAtEndOfTurn() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(3);
    }

    @Test
    void castingFlameshapeTriggersGandalfAsANoncreatureSpell() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        GandalfGoblinsBaneFlameshape adventure = new GandalfGoblinsBaneFlameshape();
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(adventure.getId()).faceDown()).isFalse();
    }

    @Test
    void flameshapeAllowsLandPlayWithOwnWizardButNotOpponentsWizard() {
        Card first = new Island();
        Card second = new Island();
        GandalfGoblinsBaneFlameshape adventure = new GandalfGoblinsBaneFlameshape();
        harness.addToBattlefield(player2, new GandalfGoblinsBaneFlameshape());
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, adventure.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, first.getId());

        assertThat(gd.findExiledCard(first.getId())).isNull();
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
    }

    @Test
    void flameshapeWithOnlyOneLibraryCardExilesThatCard() {
        Card onlyCard = new Island();
        GandalfGoblinsBaneFlameshape adventure = new GandalfGoblinsBaneFlameshape();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(onlyCard.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(adventure.getId()).faceDown()).isFalse();
    }

    @Test
    void flameshapePermissionStopsAndReturnsWhenWizardControlChanges() {
        Card exiledSpell = new Shock();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new SageOfFables());
        harness.setLibrary(player1, List.of(exiledSpell, new Island()));
        harness.setHand(player1, List.of(new GandalfGoblinsBaneFlameshape()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wizard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wizard);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledSpell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiledSpell.getId()).faceDown()).isTrue();

        harness.addToBattlefield(player1, new SageOfFables());
        harness.castFromExile(player1, exiledSpell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledSpell.getId())).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
