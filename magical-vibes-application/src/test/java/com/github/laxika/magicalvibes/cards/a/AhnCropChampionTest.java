package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AhnCropChampion.class, Colossapede.class})
class AhnCropChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addCreatureReady(player1, new AhnCropChampion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting untaps other creatures and keeps the Champion exerted")
    void exertUntapsOthers() {
        Permanent champion = addCreatureReady(player1, new AhnCropChampion());
        Permanent otherCreature = addCreatureReady(player1, new Colossapede());
        otherCreature.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(champion.isTapped()).isTrue();
        assertThat(champion.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves other creatures tapped")
    void decliningExertDoesNothing() {
        Permanent champion = addCreatureReady(player1, new AhnCropChampion());
        Permanent otherCreature = addCreatureReady(player1, new Colossapede());
        otherCreature.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(champion.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is paid before the untap trigger resolves")
    void exertPreventsUntapBeforeTriggerResolution() {
        Permanent champion = addCreatureReady(player1, new AhnCropChampion());
        Permanent other = addCreatureReady(player1, new Colossapede());
        other.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(champion.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Exert skips only the next untap step of the attacking player")
    void exertSkipsOneUntapStep() {
        Permanent champion = addCreatureReady(player1, new AhnCropChampion());
        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(champion.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(champion.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(champion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert untaps fellow attackers but not opposing creatures")
    void exertUntapsOtherAttackerOnlyOnControllersBattlefield() {
        Permanent champion = addCreatureReady(player1, new AhnCropChampion());
        Permanent other = addCreatureReady(player1, new Colossapede());
        Permanent opponent = addCreatureReady(player2, new Colossapede());
        opponent.tap();

        declareAttackers(List.of(0, 1));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(champion.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Changing control after exert does not freeze the new controller's untap step")
    void exertDoesNotPreventNewControllersUntap() {
        Permanent champion = addCreatureReady(player1, new AhnCropChampion());
        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(champion);
        gd.playerBattlefields.get(player2.getId()).add(champion);
        harness.performUntapStep(player2);

        assertThat(champion.isTapped()).isFalse();
    }
}
