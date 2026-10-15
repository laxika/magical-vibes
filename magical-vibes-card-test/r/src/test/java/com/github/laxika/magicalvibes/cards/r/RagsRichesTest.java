package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagsRiches.class, HillGiant.class, GrizzlyBears.class, GiantSpider.class})
class RagsRichesTest extends BaseCardTest {

    @Test
    @DisplayName("Rags gives -2/-2 to all creatures")
    void ragsDebuffsAllCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent own = giant(player1);
        Permanent opp = giant(player2);
        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(opp.getEffectivePower()).isEqualTo(1);
        assertThat(opp.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Rags");
    }

    @Test
    @DisplayName("Rags kills 2/2 creatures and wears off at end of turn")
    void ragsKillsSmallAndWearsOff() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.setHand(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(giant(player1).getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(giant(player1).getEffectivePower()).isEqualTo(3);
        assertThat(giant(player1).getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Riches with one opponent creature auto-steals it and exiles")
    void richesAutoStealsSingleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.stolenCreatures).containsEntry(bears.getId(), player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rags") || c.getName().equals("Riches"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rags"));
    }

    @Test
    @DisplayName("Riches prompts opponent to choose among multiple creatures")
    void richesPromptsChoiceThenSteals() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setGraveyard(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.OpponentChoosesCreatureYouGainControl.class);

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Riches does nothing when opponent has no creatures")
    void richesNoCreatures() {
        harness.setGraveyard(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rags"));
    }

    @Test
    @DisplayName("Riches requires sorcery timing")
    void richesRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rags does not debuff creatures entering after it resolves")
    void ragsDoesNotAffectLaterCreatures() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent affected = findPermanent(player2, "Giant Spider");
        assertThat(affected.getEffectivePower()).isZero();
        assertThat(affected.getEffectiveToughness()).isEqualTo(2);

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Riches control persists after cleanup")
    void richesControlPersistsAfterCleanup() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setGraveyard(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(spider);
    }

    @Test
    @DisplayName("Riches lets the opponent choose only their own creatures")
    void richesRejectsChoosingControllersCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setGraveyard(player1, List.of(new RagsRiches()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, own.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own, chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other).doesNotContain(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rags"));
    }

    private Permanent giant(Player player) {
        return findPermanent(player, "Hill Giant");
    }
}
