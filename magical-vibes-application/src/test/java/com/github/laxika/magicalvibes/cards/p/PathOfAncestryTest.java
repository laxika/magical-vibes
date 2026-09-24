package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PathOfAncestry.class)
class PathOfAncestryTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.addToBattlefield(player1, new PathOfAncestry());

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void producesCommanderIdentityManaAndTagsIt() {
        gd.playerCommanders.put(player1.getId(), List.of(commander()));
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("WHITE", "BLACK", "RED");
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getPathOfAncestryManaTotal()).isEqualTo(1);
    }

    @Test
    void matchingCreatureSpellCausesScry() {
        gd.playerCommanders.put(player1.getId(), List.of(commander()));
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setLibrary(player1, List.of(new Card()));
        harness.setHand(player1, List.of(creature(CardSubtype.VAMPIRE)));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void nonmatchingCreatureSpellDoesNotCauseScry() {
        gd.playerCommanders.put(player1.getId(), List.of(commander()));
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(creature(CardSubtype.ELF)));

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    private static Card commander() {
        Card commander = new Card();
        commander.setName("Test Vampire Commander");
        commander.setType(CardType.CREATURE);
        commander.setColors(List.of(CardColor.WHITE, CardColor.BLACK, CardColor.RED));
        commander.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK, CardColor.RED));
        commander.setSubtypes(List.of(CardSubtype.VAMPIRE));
        return commander;
    }

    private static Card creature(CardSubtype subtype) {
        Card creature = new Card();
        creature.setName("Test " + subtype.getDisplayName());
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{R}");
        creature.setColor(CardColor.RED);
        creature.setColors(List.of(CardColor.RED));
        creature.setSubtypes(List.of(subtype));
        creature.setPower(1);
        creature.setToughness(1);
        return creature;
    }
}
