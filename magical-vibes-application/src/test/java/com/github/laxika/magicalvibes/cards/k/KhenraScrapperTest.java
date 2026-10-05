package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhenraScrapper.class, KefnetsLastWord.class})
class KhenraScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +2/+0 until end of turn")
    void exertBoosts() {
        Permanent scrapper = addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scrapper)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent scrapper = addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scrapper.isTapped()).isTrue();
        assertThat(scrapper.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent scrapper = addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scrapper)).isEqualTo(3);
        assertThat(scrapper.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is chosen during attacker declaration before players receive priority")
    void exertChoiceIsPartOfDeclaringAttackers() {
        addReadyScrapper(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("The untap restriction is paid before the exert bonus resolves")
    void exertIsPaidBeforeBonusResolves() {
        Permanent scrapper = addReadyScrapper(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(scrapper.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(2);
            assertThat(gd.stack).hasSize(1);
        });
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exert skips one of the exerting player's untap steps")
    void exertSkipsOnlyNextUntap() {
        Permanent scrapper = addReadyScrapper(player1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(scrapper.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(scrapper.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(scrapper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The exert bonus expires at end of turn")
    void exertBonusExpires() {
        Permanent scrapper = addReadyScrapper(player1);
        harness.setLibrary(player2, List.of(new KhenraScrapper()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scrapper)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exert does not stop untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent scrapper = addReadyScrapper(player1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KefnetsLastWord()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveSorcery(player2, 0, scrapper.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scrapper);
        harness.performUntapStep(player2);
        assertThat(scrapper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Menace forbids a single blocker")
    void menaceRejectsSingleBlocker() {
        addReadyScrapper(player1);
        addReadyScrapper(player2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void menaceAllowsTwoBlockers() {
        addReadyScrapper(player1);
        Permanent first = addReadyScrapper(player2);
        Permanent second = addReadyScrapper(player2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private Permanent addReadyScrapper(Player player) {
        return addCreatureReady(player, new KhenraScrapper());
    }
}
