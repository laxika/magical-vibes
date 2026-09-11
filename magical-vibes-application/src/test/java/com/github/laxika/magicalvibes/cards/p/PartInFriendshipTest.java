package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PartInFriendship.class, Forest.class, GrizzlyBears.class, Shock.class})
class PartInFriendshipTest extends BaseCardTest {

    @Test
    void putsRevealedCreatureOntoBattlefieldWhenItsManaValueIsWithinLandCount() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card revealedBeforeCreature = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealedBeforeCreature, creature));

        killCreature(player2, player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealedBeforeCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void putsRevealedCreatureIntoHandWhenItsManaValueExceedsLandCount() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        killCreature(player2, player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCreature, secondCreature));

        killCreature(player2, player1);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature);

        UUID remainingCreatureId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE))
                .map(Permanent::getId)
                .findFirst()
                .orElseThrow();
        killCreatureById(player2, remainingCreatureId);

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstCreature)
                .doesNotContain(secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).contains(secondCreature);
    }

    private void killCreature(com.github.laxika.magicalvibes.model.Player caster,
            com.github.laxika.magicalvibes.model.Player targetController) {
        UUID targetId = harness.getPermanentId(targetController, "Grizzly Bears");
        killCreatureById(caster, targetId);
    }

    private void killCreatureById(com.github.laxika.magicalvibes.model.Player caster,
            UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
