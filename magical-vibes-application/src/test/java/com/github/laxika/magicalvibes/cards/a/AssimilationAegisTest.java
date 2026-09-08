package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssimilationAegis.class, Disenchant.class, GrizzlyBears.class, HillGiant.class})
class AssimilationAegisTest extends BaseCardTest {

    @Test
    void entersAndExilesUpToOneTargetCreatureUntilItLeaves() {
        Permanent exiledTarget = addCreatureReady(player2, new HillGiant());
        Permanent aegis = castAndExile(exiledTarget);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(exiledTarget);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledTarget.getOriginalCard());

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, aegis.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aegis);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId()
                        .equals(exiledTarget.getOriginalCard().getId()));
    }

    @Test
    void attachedCreatureBecomesCopyAndRevertsWhenReequipped() {
        Permanent exiledTarget = addCreatureReady(player2, new HillGiant());
        Permanent firstHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent aegis = castAndExile(exiledTarget);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aegis),
                null, firstHost.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstHost)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstHost)).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aegis),
                null, secondHost.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstHost)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstHost)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondHost)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondHost)).isEqualTo(3);
    }

    @Test
    void choosesAmongMultipleCreatureCardsWhenTheTriggerResolves() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent aegis = harness.addToBattlefieldAndReturn(player1, new AssimilationAegis());
        HillGiant first = new HillGiant();
        GrizzlyBears second = new GrizzlyBears();
        gd.addToExile(player1.getId(), first, aegis.getId());
        gd.addToExile(player1.getId(), second, aegis.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aegis),
                null, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AssimilationAegisCopyChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
    }

    private Permanent castAndExile(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AssimilationAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AssimilationAegis)
                .findFirst()
                .orElseThrow();
    }
}
