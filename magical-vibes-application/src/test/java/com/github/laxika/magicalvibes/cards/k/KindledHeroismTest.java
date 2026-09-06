package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindledHeroism.class, Forest.class, GrizzlyBears.class})
class KindledHeroismTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0 and first strike, then scries 1")
    void boostsAndScries() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new KindledHeroism()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 finishes resolving after ordering the library")
    void scryFinishesResolving() {
        Permanent target = addCreature();
        Card bottom = new Forest();
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new KindledHeroism()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bottom);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kindled Heroism");
    }

    @Test
    @DisplayName("The boost and first strike wear off at cleanup")
    void temporaryEffectsWearOffAtCleanup() {
        Permanent target = addCreature();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new KindledHeroism()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new KindledHeroism()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature() {
        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(target);
        return target;
    }
}
