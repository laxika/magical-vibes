package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MtendaGriffin;
import com.github.laxika.magicalvibes.cards.p.PrismaticLace;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulRend.class, MtendaGriffin.class, FeralShadow.class})
class SoulRendTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a white creature")
    void destroysWhiteCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new MtendaGriffin());
        harness.setHand(player1, List.of(new SoulRend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Mtenda Griffin")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mtenda Griffin"));
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void destructionCannotBeRegenerated() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new MtendaGriffin());
        griffin.setRegenerationShield(1);
        harness.setHand(player1, List.of(new SoulRend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Mtenda Griffin")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mtenda Griffin"));
    }

    @Test
    @DisplayName("A non-white creature may be targeted but survives")
    void nonWhiteCreatureSurvives() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player2, new FeralShadow());
        harness.setHand(player1, List.of(new SoulRend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, shadow.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Feral Shadow")).hasSize(1);
    }

    @Test
    @DisplayName("Schedules a draw at the next upkeep even when the target is not white")
    void schedulesDrawAtNextUpkeep() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player2, new FeralShadow());
        harness.setHand(player1, List.of(new SoulRend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, shadow.getId());
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @CardUsed(PrismaticLace.class)
    @DisplayName("Checks the target's color when it resolves")
    void checksColorAtResolution() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new MtendaGriffin());
        harness.setHand(player1, List.of(new SoulRend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new PrismaticLace()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, griffin.getId());
        harness.handleListChoice(player2, "BLACK");
        harness.handleListChoice(player2, "DONE");

        assertThat(gqs.getEffectiveColors(gd, griffin)).containsExactly(CardColor.BLACK);

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Mtenda Griffin")).hasSize(1);
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SoulRend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
