package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SHIELDSpyKit.class, GrizzlyBears.class, Forest.class})
class SHIELDSpyKitTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spyKit = addSpyKitReady(player1);
        spyKit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking alone untaps the equipped creature and starts scry 1")
    void attacksAloneUntapsAndScries() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spyKit = addSpyKitReady(player1);
        spyKit.setAttachedTo(creature.getId());
        Card originalTop = new Forest();
        harness.setLibrary(player1, List.of(originalTop, new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(originalTop);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger the Spy Kit")
    void doesNotTriggerWhenNotAttackingAlone() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spyKit = addSpyKitReady(player1);
        spyKit.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip attaches the Spy Kit to a creature you control")
    void equipAttachesToCreature() {
        Permanent spyKit = addSpyKitReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spyKit.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addSpyKitReady(Player player) {
        Permanent permanent = new Permanent(new SHIELDSpyKit());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
