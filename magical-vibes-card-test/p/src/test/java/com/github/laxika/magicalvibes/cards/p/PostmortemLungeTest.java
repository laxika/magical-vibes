package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GethsVerdict;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PostmortemLunge.class, GrizzlyBears.class, SerraAngel.class, Memnite.class, GethsVerdict.class})
class PostmortemLungeTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Postmortem Lunge puts it on the stack with graveyard target")
    void castingPutsOnStack() {
        Card target = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B/P} — X=2, base cost {B/P}

        harness.castSorcery(player1, 0, 2, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(entry.getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving returns creature to battlefield with haste")
    void resolvesAndReturnsCreatureWithHaste() {
        Card target = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Creature should be removed from graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        // Creature should have haste
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        // Creature should be marked for exile at end step
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).contains(new DelayedPermanentAction(creature.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));
    }

    @Test
    @DisplayName("Creature is exiled at the beginning of the next end step")
    void creatureExiledAtEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        // Creature should no longer be on the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Creature should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target creature with mana value different from X")
    void cannotTargetWrongManaValue() {
        Card target = new SerraAngel(); // mana value 5
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2, but Serra Angel has mana value 5

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value must equal X");
    }

    @Test
    @DisplayName("Can target creature with mana value 0 when X=0")
    void canTargetManaValueZero() {
        Card target = new Memnite();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Memnite");
        harness.assertNotInGraveyard(player1, "Memnite");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Memnite"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fizzles if target leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Postmortem Lunge goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Postmortem Lunge");
    }

    @Test
    @DisplayName("Cannot target a noncreature card even when its mana value equals X")
    void cannotTargetNoncreature() {
        Card target = new PostmortemLunge();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X zero can be paid with no mana and two life")
    void zeroXWithLifePayment() {
        Card target = new Memnite();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Memnite");
    }

    @Test
    @DisplayName("Phyrexian mana can be paid with two life while X is paid with mana")
    void paysPhyrexianManaWithLife() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A returned creature sacrificed before the end step stays in the graveyard")
    void sacrificedCreatureIsNotExiled() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge(), new GethsVerdict()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
    }
}
