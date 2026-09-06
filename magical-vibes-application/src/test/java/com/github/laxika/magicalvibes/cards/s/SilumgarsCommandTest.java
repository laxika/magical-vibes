package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilumgarsCommand.class, ChandraNalaar.class, GrizzlyBears.class, Spellbook.class})
class SilumgarsCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and returns a permanent")
    void countersNoncreatureSpellAndReturnsPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Spellbook spellbook = new Spellbook();
        harness.setHand(player1, List.of(spellbook));

        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 1}, spellbook.getId(),
                List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gives a creature -3/-3 and destroys a planeswalker")
    void weakensCreatureAndDestroysPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{2, 3}, null,
                List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature, planeswalker);
    }

    @Test
    @DisplayName("Rejects a creature spell as the target of the counter mode")
    void counterModeRejectsCreatureSpell() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player2, 0, 2,
                new int[]{0, 2}, spell.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCommandMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
