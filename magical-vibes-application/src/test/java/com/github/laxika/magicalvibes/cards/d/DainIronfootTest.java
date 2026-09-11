package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DainIronfoot.class, GrizzlyBears.class, LeoninScimitar.class})
class DainIronfootTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates an Axe and attaches it to a creature you control")
    void entersCreatesAndAttachesAxe() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DainIronfoot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        Permanent dain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof DainIronfoot)
                .findFirst().orElseThrow();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                bears.getId(), dain.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent axe = findPermanent(player1, "Axe");
        assertThat(axe.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB target selection excludes creatures an opponent controls")
    void onlyOwnCreaturesAreLegalAttachmentTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DainIronfoot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        Permanent dain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof DainIronfoot)
                .findFirst().orElseThrow();
        assertThat(choice.validIds())
                .containsExactly(dain.getId())
                .doesNotContain(harness.getPermanentId(player2, "Grizzly Bears"));
    }

    @Test
    @DisplayName("Attacking equipped creatures gain double strike until end of turn")
    void attackingEquippedCreaturesGainDoubleStrike() {
        Permanent dain = addCreatureReady(player1, new DainIronfoot());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent dainEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent bearsEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        dainEquipment.setAttachedTo(dain.getId());
        bearsEquipment.setAttachedTo(bears.getId());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, dain, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dain, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

}
