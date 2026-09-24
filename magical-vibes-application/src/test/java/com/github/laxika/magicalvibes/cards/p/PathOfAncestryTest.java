package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfAncestry.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class PathOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new PathOfAncestry()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findPermanent(player1, "Path of Ancestry").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana spent on a creature sharing a type with the commander scries")
    void matchingCreatureSpellScries() {
        gd.playerCommanders.put(player1.getId(), List.of(new GrizzlyBears()));
        Permanent path = addReadyPath();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Path of Ancestry"));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(path.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature without a shared type does not trigger scry")
    void differentCreatureTypeDoesNotScry() {
        gd.playerCommanders.put(player1.getId(), List.of(new GrizzlyBears()));
        addReadyPath();
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Choosing a color for a multicolor commander preserves the spell trigger")
    void multicolorCommanderChoicePreservesTrigger() {
        Card commander = new Card();
        commander.setName("Multicolor Bear Commander");
        commander.setType(CardType.CREATURE);
        commander.setSubtypes(List.of(CardSubtype.BEAR));
        commander.setColorIdentity(List.of(CardColor.GREEN, CardColor.RED));
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.playerCommanders.put(player1.getId(), List.of(commander));

        addReadyPath();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("GREEN", "RED");
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Path of Ancestry"));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    private Permanent addReadyPath() {
        Permanent path = new Permanent(new PathOfAncestry());
        path.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(path);
        return path;
    }
}
