package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlibouAncientWitness.class, Ornithopter.class, AccordersShield.class, GrizzlyBears.class, Forest.class})
class AlibouAncientWitnessTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Gives other artifact creatures you control haste")
    void givesOtherArtifactCreaturesHaste() {
        Permanent alibou = addCreatureReady(player1, new AlibouAncientWitness());
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent nonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, alibou, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonArtifactCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Deals damage and scries based on tapped artifacts after an artifact creature attacks")
    void attacksTriggerDamageAndScry() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new Ornithopter());
        Permanent tappedArtifact = new Permanent(new AccordersShield());
        tappedArtifact.setSummoningSick(false);
        tappedArtifact.tap();
        gd.playerBattlefields.get(player1.getId()).add(tappedArtifact);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only a nonartifact creature attacks")
    void doesNotTriggerForNonartifactAttacker() {
        addCreatureReady(player1, new AlibouAncientWitness());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Alibou, Ancient Witness"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
