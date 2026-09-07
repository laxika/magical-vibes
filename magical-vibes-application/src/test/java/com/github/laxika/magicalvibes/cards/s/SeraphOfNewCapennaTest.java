package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeraphOfNewCapenna.class, SeraphOfNewPhyrexia.class, GrizzlyBears.class, Ornithopter.class})
class SeraphOfNewCapennaTest extends BaseCardTest {

    @Test
    void activatesWithPhyrexianManaAndTransformsAtSorcerySpeed() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seraph.isTransformed()).isTrue();
    }

    @Test
    void transformedSeraphMaySacrificeAnotherCreatureOrArtifactForPlusTwoPlusOne() {
        Permanent seraph = addTransformedSeraph();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), ornithopter.getId());

        harness.handlePermanentChosen(player1, ornithopter.getId());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ornithopter.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    void transformedSeraphCannotSacrificeItselfAndDecliningDoesNotBoost() {
        Permanent seraph = addTransformedSeraph();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seraph, bears);
    }

    private Permanent addTransformedSeraph() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfNewCapenna());
        seraph.setCard(seraph.getCard().getBackFaceCard());
        seraph.setTransformed(true);
        return seraph;
    }

}
