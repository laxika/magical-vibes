package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpikedRipsaw.class, Forest.class, GrizzlyBears.class})
class SpikedRipsawTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ripsaw = addRipsawReady(player1);
        ripsaw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking offers sacrificing a Forest")
    void attackingOffersSacrificingForest() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ripsaw = addRipsawReady(player1);
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(forest.getId());
    }

    @Test
    @DisplayName("Sacrificing a Forest gives the equipped creature trample until end of turn")
    void sacrificingForestGrantsTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ripsaw = addRipsawReady(player1);
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Declining the Forest sacrifice does not grant trample")
    void decliningSacrificeDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ripsaw = addRipsawReady(player1);
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger does not fire while the Equipment is unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addRipsawReady(player1);
        harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addRipsawReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent ripsaw = new Permanent(new SpikedRipsaw());
        ripsaw.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ripsaw);
        return ripsaw;
    }
}
