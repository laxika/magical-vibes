package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NarsetTranscendent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilumgarsCommand.class, ChandraNalaar.class, GrizzlyBears.class, Spellbook.class,
        ColossodonYearling.class, NarsetTranscendent.class, SpidersilkNet.class})
class SilumgarsCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and returns a permanent")
    void countersNoncreatureSpellAndReturnsPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Spellbook spellbook = new Spellbook();

        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player2);

        harness.castFromHand(player1, spellbook, "{0}");
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

        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player2);

        harness.castFromHand(player1, spell, "{1}{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player2, 0, 2,
                new int[]{0, 2}, spell.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The bounce and weakening modes can target the same creature")
    void bounceAndWeakeningCanShareTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 2}, null,
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Colossodon Yearling");
        harness.assertNotOnBattlefield(player2, "Colossodon Yearling");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A shared planeswalker target is returned before the destroy mode")
    void bounceAndDestroyCanSharePlaneswalkerTarget() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NarsetTranscendent());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, null,
                List.of(planeswalker.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Narset Transcendent");
        harness.assertNotOnBattlefield(player2, "Narset Transcendent");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a noncreature permanent and weakens only the creature until cleanup")
    void weakeningExpiresAtEndOfTurn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 2}, null,
                List.of(artifact.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spidersilk Net");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rejects a noncreature permanent for the weakening mode")
    void weakeningRejectsNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2,
                new int[]{1, 2}, null, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature for the planeswalker destruction mode")
    void destructionRejectsNonplaneswalker() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2,
                new int[]{1, 3}, null, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an artifact while weakening a separate creature")
    void counterAndWeakeningUseSeparateTargets() {
        SpidersilkNet spell = new SpidersilkNet();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player2);
        harness.castFromHand(player1, spell, "{0}");
        harness.passPriority(player1);

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 2}, spell.getId(),
                List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spidersilk Net");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters a noncreature spell and destroys a separate planeswalker")
    void counterAndDestructionUseSeparateTargets() {
        SpidersilkNet spell = new SpidersilkNet();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new NarsetTranscendent());
        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player2);
        harness.castFromHand(player1, spell, "{0}");
        harness.passPriority(player1);

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 3}, spell.getId(),
                List.of(planeswalker.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spidersilk Net");
        harness.assertInGraveyard(player1, "Narset Transcendent");
        harness.assertNotOnBattlefield(player1, "Narset Transcendent");
    }

    @Test
    @DisplayName("Still destroys its legal planeswalker target when the bounce target leaves")
    void resolvesRemainingLegalTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NarsetTranscendent());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        harness.setHand(player2, List.of(new SilumgarsCommand()));
        addCommandMana(player1);
        addCommandMana(player2);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, null,
                List.of(artifact.getId(), planeswalker.getId()));
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{1, 2}, null,
                List.of(artifact.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spidersilk Net");
        harness.assertInGraveyard(player2, "Narset Transcendent");
        harness.assertNotOnBattlefield(player2, "Narset Transcendent");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires two distinct modes rather than the same mode twice")
    void rejectsChoosingSameModeTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new SilumgarsCommand()));
        addCommandMana(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2,
                new int[]{1, 1}, null, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCommandMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
